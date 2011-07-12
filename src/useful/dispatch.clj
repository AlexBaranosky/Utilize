(ns useful.dispatch
  (:use [useful.map :only [into-map]]
        [useful.fn :only [any]]
        [useful.utils :only [verify]]))

(defn get-sub-type [hierarchy ns]
  (let [sub-type (get hierarchy ns)]
    (verify (not= sub-type ns) "a node type cannot have itself as a sub-type")
    sub-type))

(defn dispatcher
  "Returns a function that dispatches using the given dispatch function to determine the
  namespace and function to call."
  [dispatch-fn & options]
  (let [{:keys [hierarchy wrap default]} (into-map options)
        wrap (or wrap identity)]
    (fn [& args]
      (let [fname   (apply dispatch-fn args)
            default (or default
                        (with-meta (fn [& args]
                                     (throw (IllegalArgumentException. (str "cannot resolve function: " fname))))
                          {:no-wrap true}))]
        (loop [[ns method] (map symbol ((juxt namespace name) (symbol fname)))]
          (if-let [f (if ns
                       (try (require ns)
                            (ns-resolve ns method)
                            (catch java.io.FileNotFoundException e))
                       default)]
            (let [wrap (if (:no-wrap (meta f))
                         identity
                         wrap)]
                  (apply (wrap f) args))
            (recur [(get-sub-type hierarchy ns) method])))))))

(defmacro defdispatch
  "Defines a function that dispatches using the given dispatch function to determine the
  namespace and function to call."
  {:arglists '([name docstring? attr-map? dispatch-fn & options])}
  [name & options]
  (let [[defn-options [dispatch-fn & options]] (split-with (any string? map?) options)]
    `(let [dispatcher# (dispatcher ~dispatch-fn ~@options)]
       (defn ~name ~@defn-options [& args#]
         (apply dispatcher# args#)))))