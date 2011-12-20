(ns utilize.map-test
  (:use utilize.controlflow
        midje.sweet))

(fact "checks each pred against the result of the first expression, returning if it finds a match" 

  (pred-cond "abcde" 
    #(.contains % "xyz") "contains 'xyz'" 
    string? "string"
    :else "neither") => "string"

  (pred-cond 1 
    even? "even" 
    string? "string"
    :else "neither") => "neither"
  
  "Don't need an :else"
  (pred-cond 1 
    even? "even") => nil)