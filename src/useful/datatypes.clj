(ns useful.datatypes
  (:use [useful.map :only [position into-map update]]
        [useful.fn :only [fix]])
  (:require [clojure.string :as s])
  (:import (java.lang.reflect Field)
           (clojure.lang Compiler$LocalBinding)))

(defn- normalize-field-name [field]
  (-> (name field)
      (s/replace #"_QMARK_" "?")
      (s/replace #"_"       "-")
      symbol))

(defn- ^Class coerce-class
  "Get a Class object from either a Symbol (by resolving it) or a Class."
  [type]
  (fix type symbol? resolve))

(defn- record-fields
  "Uses reflection to get the declared fields passed to the defrecord call for type. If called on a
   non-record, the behavior is undefined."
  [type]
  (->> (.getDeclaredFields (coerce-class type))
       (remove #(java.lang.reflect.Modifier/isStatic (.getModifiers ^Field %)))
       (remove #(let [name (.getName ^Field %)]
                  (and (not (#{"__extmap" "__meta"} name))
                       (.startsWith name "__"))))
       (map #(symbol (normalize-field-name (.getName ^Field %))))))

(defmacro make-record
  "Construct a record given a pairs of lists and values. Mapping fields into constuctor arguments is
  done at compile time, so this is more efficient than creating an empty record and calling merge."
  [type & attrs]
  (let [fields (record-fields type)
        index  (position fields)
        vals   (reduce (fn [vals [field val]]
                         (if-let [i (index (normalize-field-name field))]
                           (assoc vals i val)
                           (assoc-in vals
                             [(index '--extmap) (keyword field)] val)))
                       (vec (repeat (count fields) nil))
                       (into-map attrs))]
    `(new ~type ~@vals)))

(defn- type-hint [form &env fn-name]
  (or (:tag (meta form))
      (let [^Compiler$LocalBinding binding (get &env form)]
        (and binding (.hasJavaClass binding) (.getJavaClass binding)))
      (throw (Exception. (str "type hint required on record to use " fn-name)))))

(defmacro assoc-record
  "Assoc attrs into a record. Mapping fields into constuctor arguments is done at compile time,
   so this is more efficient than calling assoc on an existing record."
  [record & attrs]
  (let [r      (gensym 'record)
        type   (type-hint record &env 'assoc-record)
        fields (record-fields type)
        index  (position fields)
        vals   (reduce (fn [vals [field val]]
                         (if-let [i (index (normalize-field-name field))]
                           (assoc vals i val)
                           (assoc-in vals
                             [(index '--extmap) (keyword field)] val)))
                       (vec (map #(list '. r %) fields))
                       (into-map attrs))]
    `(let [~r ~record]
       (new ~type ~@vals))))

(defmacro update-record
  "Construct a record given a list of forms like (update-fn record-field & args). Mapping fields
  into constuctor arguments is done at compile time, so this is more efficient than calling assoc on
  an existing record."
  [record & forms]
  (let [r      (gensym 'record)
        type   (type-hint record &env 'update-record)
        fields (record-fields type)
        index  (position fields)
        vals   (reduce (fn [vals [f field & args]]
                         (if-let [i (index (normalize-field-name field))]
                           (assoc vals
                             i (apply list f (get vals i) args))
                           (let [i (index '--extmap)]
                             (assoc vals
                               i (apply list `update (get vals i) (keyword field) args)))))
                       (vec (map #(list '. r %) fields))
                       forms)]
    `(let [~r ~record]
       (new ~type ~@vals))))

(defmacro record-accessors
  "Defines optimized macro accessors using interop and typehints for all fields in the given records."
  [& types]
  `(do ~@(for [type  types
               :let [tag (symbol (.getName (coerce-class type)))]
               field (record-fields type)]
           `(defmacro ~field [~'record]
              (with-meta
                (list '. (with-meta ~'record {:tag '~tag})
                      '~field)
                (meta ~'&form))))))
