(ns utilize.string
  (:require [clojure.string :as str]))

(defn camelize [#^String string]
  (str/replace string
             #"[-_](\w)"
             (comp str/upper-case second)))

 (defn classify [#^String string]
   (apply str (map str/capitalize
                   (str/split string #"[-_]"))))

(defn- from-camel-fn [#^String separator]
  (fn [#^String string]
    (-> string
        (str/replace #"^[A-Z]+" str/lower-case)
        (str/replace #"_?([A-Z]+)"
                   (comp (partial str separator)
                         str/lower-case second))
        (str/replace #"-|_" separator))))

(def dasherize (from-camel-fn "-"))
(def underscore (from-camel-fn "_"))

(defn pluralize
  "Return a pluralized phrase, appending an s to the singular form if no plural is provided.
  For example:
     (plural 5 \"month\") => \"5 months\"
     (plural 1 \"month\") => \"1 month\"
     (plural 1 \"radius\" \"radii\") => \"1 radius\"
     (plural 9 \"radius\" \"radii\") => \"9 radii\""
  [num #^String singular & [plural]]
  (str num " " (if (= 1 num) singular (or plural (str singular "s")))))

(defn but-last-str [#^String s n]
   (if (> n (.length s))
       ""
      (.substring s 0 (- (.length s) n))))

(defn ordinal-to-int [#^String ord]
  (let [digits (but-last-str ord 2)]
    (Integer/parseInt digits)))

(defn ordinalize [int]
  (if (contains? #{11 12 13} (mod int 100))
    (str int "th")
    (condp = (mod int 10)
      1 (str int "st")
      2 (str int "nd")
      3 (str int "rd")
      (str int "th"))))

(defn lowercase-keyword [#^String s]
  (keyword (.toLowerCase (str/replace s " " "-"))))
