(ns utilize.cli-test
  (:use clojure.test utilize.cli))

(deftest test-parse-opts
  (is (= {:foo ["a"] :bar [""]} (parse-opts ["--foo=a" "--bar"]))))
