(ns utilize.regex-test
  (:use midje.sweet utilize.regex))

(fact "can get a seq of only the matches - not the captures"
  (re-match-seq #"\w+(\d+)" "abc def123 xyz tuv789 rad") => ["def123" "tuv789"])

(fact "gives the captures from a given regex, matching
       the regex against the entire string"
   (re-captures #"(\d) (\d) (\d)" "1 2 4") => ["1" "2" "4"]
   (re-captures #"(\d)" "a b c") => [])