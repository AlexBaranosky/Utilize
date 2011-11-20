(ns utilize.testutils
  (:import [org.joda.time DateTimeUtils]
           [org.joda.time.base BaseDateTime]))

(defn do-at* [^BaseDateTime base-date-time f]
  (DateTimeUtils/setCurrentMillisFixed (.getMillis base-date-time))
  (try
    (f)
    (finally (DateTimeUtils/setCurrentMillisSystem))))

(defmacro do-at [^BaseDateTime base-date-time & body]
  "like clojure.core.do except evalautes the expression at the given date-time"
  `(do-at* ~base-date-time
    (fn [] ~@body)))