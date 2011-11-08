(ns utilize.testutils-test
  (:use utilize.testutils midje.sweet)
  (:import [org.joda.time DateMidnight]))

(fact "freezes time at given date then returns to normal afterward"
  (DateMidnight.) =not=> (DateMidnight. 2000 1 1)
  (do-at* (DateMidnight. 2000 1 1)
    (fn [] (DateMidnight.))) => (DateMidnight. 2000 1 1)
  (DateMidnight.) =not=> (DateMidnight. 2000 1 1))

(fact "there's a macro for running some code at a certain time"
  (DateMidnight.) =not=> (DateMidnight. 2000 1 1)
  (do-at (DateMidnight. 2000 1 1)
    (DateMidnight.)
    (DateMidnight.)) => (DateMidnight. 2000 1 1)
  (DateMidnight.) =not=> (DateMidnight. 2000 1 1))

(fact "when body throws exception, we always make sure to put the time back to normal"
  (DateMidnight.) =not=> (DateMidnight. 2000 1 1)
  (do-at (DateMidnight. 2000 1 1)
    (throw (RuntimeException. "boom"))) => (throws RuntimeException "boom")
  (DateMidnight.) =not=> (DateMidnight. 2000 1 1))