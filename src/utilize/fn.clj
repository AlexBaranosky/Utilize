(ns utilize.fn)

(def ! complement)

(defn decorate
  "Return a function f such that (f x) => [x (f1 x) (f2 x) ...]."
  [& fs]
  (apply juxt identity fs))

(defn annotate
  "A vector of [x (f1 x) (f2 x) ...]."
  [x & fs]
  ((apply decorate fs) x))

(defn fix
  "Walk through clauses, a series of predicate/transform pairs. The
  first predicate that x satisfies has its transformation clause
  called on x. Predicates or transforms may be values (eg true or nil)
  rather than functions; these will be treated as functions that
  return that value.

  The last \"pair\" may be only a transform with no pred: in that case it
  is unconditionally used to transform x, if nothing previously matched.

  If no predicate matches, then x is returned unchanged."
  [x & clauses]
  (let [call #(if (ifn? %) (% x) %)]
    (first (or (seq (for [[pred & [transform :as exists?]] (partition-all 2 clauses)
                          :let [[pred transform] ;; handle odd number of clauses
                                (if exists? [pred transform] [true pred])]
                          :when (call pred)]
                      (call transform)))
               [x]))))

(defn to-fix
  "A \"curried\" version of fix, which sets the clauses once, yielding a
  function that calls fix with the specified first argument."
  [& clauses]
  (fn [x]
    (apply fix x clauses)))

(defn as-fn
  "Turn an object into a fn if it is not already by wrapping it in constantly."
  [x]
  (fix x (! ifn?) constantly))

(defmacro given
  "A macro version of fix: instead of taking multiple clauses, it treats any
  further arguments as additional args to be passed to the transform function,
  similarly to functions such as swap! and update-in."
  [x pred transform & args]
  `(fix ~x ~pred (fn [x#] (~transform x# ~@args))))

(defn any
  "Takes a list of predicates and returns a new predicate that returns true if any do."
  [& preds]
  (fn [& args]
    (some #(apply % args) preds)))

(defn all
  "Takes a list of predicates and returns a new predicate that returns true if all do."
  [& preds]
  (fn [& args]
    (every? #(apply % args) preds)))

(defn knit
  "Takes a list of functions (f1 f2 ... fn) and returns a new function F. F takes
   a collection of size n (x1 x2 ... xn) and returns a vector [(f1 x1) (f2 x2) ... (fn xn)].
   Similar to Haskell's ***, and a nice complement to juxt (which is Haskell's &&&)."
  [& fs]
  (fn [arg-coll]
    (vec (map #(% %2) fs arg-coll))))

(defn thrush
  "Takes the first argument and applies the remaining arguments to it as functions from left to right.
   This tiny implementation was written by Chris Houser. http://blog.fogus.me/2010/09/28/thrush-in-clojure-redux"
  [& args]
  (reduce #(%2 %1) args))

(defn ignoring-nils
  "Create a new version of a function which ignores all nils in its arguments:
  ((ignoring-nils +) 1 nil 2 3 nil) yields 6."
  [f]
  (fn
    ([] (f))
    ([a] (if (nil? a)
           (f)
           (f a)))
    ([a b]
       (if (nil? a)
         (if (nil? b)
           (f)
           (f b))
         (if (nil? b)
           (f a)
           (f a b))))
    ([a b & more]
       (when-let [items (seq (remove nil? (list* a b more)))]
         (apply f items)))))

(defn key-comparator
  "Given a transformation function (and optionally a direction), return a
  comparator which does its work by comparing the values of (transform x) and
  (transform y)."
  ([modifier]
     (fn [a b]
       (- (modifier a) (modifier b))))
  ([direction modifier]
     (let [f (comparator modifier)]
       (condp #(% %2) direction
         #{:desc :descending -} (comp - f)
         #{:asc :ascending +} f))))

(defn steady-state
  "Apply function f repeatedly, like iterate does, but stopping when 
   you get a repeated value"
  ([f x]
    (steady-state f x []))
  ([f x results]
    (let [calculated (f x)]
      (if (= calculated x)
        results
        (recur f calculated (conj results calculated))))))
