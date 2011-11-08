(ns utilize.testutils
  (:import [org.joda.time DateTimeUtils]))

(defn do-at* [date-time f]
  (DateTimeUtils/setCurrentMillisFixed (.getMillis date-time))
  (try
    (f)
    (finally (DateTimeUtils/setCurrentMillisSystem))))

(defmacro do-at [date-time & body]
  "like clojure.core.do except evalautes the expression at the given date-time"
  `(do-at* ~date-time
    (fn [] ~@body)))