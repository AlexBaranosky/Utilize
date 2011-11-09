(ns utilize.regex)

(def re-captures
  ^{:doc "gives the captures from a given regex, matching
          the regex against the entire string"}
  (comp rest re-matches))

(defn re-match-seq
  "A seq of allregex matches in the string"
  [re s]
  (map first (re-seq re s)))