(ns utilize.macro
  (:use [clojure.tools.macro :only [macrolet]]
        [utilize.fn :only [steady-state]]))

(defmacro anon-macro
  "Define, and then immediately use, an anonymous macro. For
example, (anon-macro [x y] `(def ~x ~y) myconst 10) expands to (def
myconst 10)."
  ([args macro-body & body]
     `(macrolet [(name# ~args ~macro-body)]
        (name# ~@body))))

(letfn [(partition-params [argvec actual-args]
          (if (some #{'&} argvec)
            [actual-args]               ; one seq with all args
            (vec (map vec (partition (count argvec) actual-args)))))]

  (defmacro macro-do
    "Wrap a list of forms with an anonymous macro, which partitions the
   forms into chunks of the right size for the macro's arglists. The
   macro's body will be called once for every N items in the args
   list, where N is the number of arguments the macro accepts. The
   result of all expansions will be glued together in a (do ...) form.

   Really, the macro is only called once, and is adjusted to expand
   into a (do ...) form, but this is probably an implementation detail
   that I'm not sure how a client could detect.

   For example,
   (macro-do [[f & args]]
             `(def ~(symbol (str \"basic-\" f))
                (partial ~f ~@args))
             [f 'test] [y 1 2 3])
   expands into (do
                 (def basic-f (partial f 'test))
                 (def basic-y (partial y 1 2 3)))"
    ([macro-args body & args]
       `(anon-macro [arg#]
                    (cons 'do
                          (for [~macro-args arg#]
                            ~body))
                    ~(partition-params macro-args args))))
  
    (defmacro macro-for
      ""
      ([macro-args body & args]
         `(anon-macro [arg#]
                      (cons 'do
                            (for [~macro-args arg#]
                              ~body))
                      ~(partition-params macro-args args)))))

;; copied from clojure.contrib.def
(defmacro ^{:dont-test "Exists in contrib, and has gross side effects anyway"}
  defalias
  "Defines an alias for a var: a new var with the same root binding (if
  any) and similar metadata. The metadata of the alias is its initial
  metadata (as provided by def) merged into the metadata of the original."
  ([name orig]
     `(do
        (alter-meta!
         (if (.hasRoot (var ~orig))
           (def ~name (.getRoot (var ~orig)))
           (def ~name))
         ;; When copying metadata, disregard {:macro false}.
         ;; Workaround for http://www.assembla.com/spaces/clojure/tickets/273
         #(conj (dissoc % :macro)
                (apply dissoc (meta (var ~orig)) (remove #{:macro} (keys %)))))
        (var ~name)))
  ([name orig doc]
     (list `defalias (with-meta name (assoc (meta name) :doc doc)) orig)))

(defmacro with-altered-var
  "Binds var-name to the result of (f current-value args) for the dynamic
  scope of body. Basically like swap! or alter, but for vars."
  [[var-name f & args] & body]
  `(binding [~var-name (~f ~var-name ~@args)]
     ~@body))

(defn macroexpand-scan
  "Gives a seq of each progressive macroexpansion of the form until fully expanded"
  [form]
  (steady-state macroexpand-1 form))

(defmacro macro-for 
  "Macroexpands the body once for each of the elements in the bindings"
  [bindings body] 
  `(let [macros# (for [~@bindings]
                     ~body)]
    `(do ~@macros#)))