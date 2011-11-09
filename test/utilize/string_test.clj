(ns utilize.string-test
  (:use utilize.string clojure.test midje.sweet))

(deftest to-camel
  (are [in out] (= out (camelize in))
       "the-string" "theString"
       "this-is-real" "thisIsReal"
       "untouched" "untouched"))

(deftest to-class
  (are [in out] (= out (classify in))
       "the-string" "TheString"
       "this-is-real" "ThisIsReal"
       "touched" "Touched"))

(deftest from-camel
  (are [in dashed underscored] (= [dashed underscored]
                                  ((juxt dasherize underscore) in))
       "setSize"          "set-size"         "set_size"
       "theURL"           "the-url"          "the_url"
       "ClassName"        "class-name"       "class_name"
       "LOUD_CONSTANT"    "loud-constant"    "loud_constant"
       "the_CRAZY_train"  "the-crazy-train"  "the_crazy_train"
       "with-dashes"      "with-dashes"      "with_dashes"
       "with_underscores" "with-underscores" "with_underscores"))

(deftest pluralize-test
  (is (= "10 dogs" (pluralize 10 "dog")))
  (is (= "1 cat" (pluralize 1 "cat")))
  (is (= "0 octopodes" (pluralize 0 "octopus" "octopodes")))
  (is (= "1 fish" (pluralize 1 "fish" "fishes"))))

(tabular
  (fact "cop off the last 'n' chars of a given string"
    (but-last-str "1234" ?amt) => ?result)

	?amt ?result
	0    "1234"
	1    "123"
	2    "12"
	3    "1"
	4    ""
	5    "")

(tabular
  (fact "convert ordinal strings to ints"
    (ordinal-to-int ?ord) => ?int)

	?ord  ?int
	"1st"  1
    "2nD"  2
    "3RD"  3
    "4Th"  4
    "5th"  5
    "6th"  6
    "7th"  7
    "8th"  8
    "9th"  9
    "10th" 10
    "11th" 11
    "12th" 12
    "13th" 13
    "14th" 14
    "15th" 15
    "16th" 16
    "17th" 17
    "18th" 18
    "19th" 19
    "20th" 20
    "21st" 21
    "22nd" 22
    "23rd" 23
    "24th" 24
    "25th" 25
    "26th" 26
    "27th" 27
    "28th" 28
    "29th" 29
    "30th" 30
    "31st" 31
    "331st" 331
    "33331st" 33331)

(tabular
  (fact "convert int to a ordinal"
    (ordinalize ?int) => ?ord)

	?int ?ord
    1  "1st"
    2  "2nd"
    3  "3rd"
    4  "4th"
    5  "5th"
    6  "6th"
    7  "7th"
    8  "8th"
    9  "9th"
    10 "10th"
    11 "11th"
    12 "12th"
    13 "13th"
    14 "14th"
    15 "15th"
    16 "16th"
    17 "17th"
    18 "18th"
    19 "19th"
    20 "20th"
    21 "21st"
    22 "22nd"
    23 "23rd"
    24 "24th"
    25 "25th"
    26 "26th"
    27 "27th"
    28 "28th"
    29 "29th"
    30 "30th"
    31 "31st"
    311 "311th"
    312 "312th"
    313 "313th"
    33331 "33331st")

(fact "can generate lower-case keywords from a string"
  (lowercase-keyword "BobCratchet") => :bobcratchet
  (lowercase-keyword "Bob Cratchet") => :bob-cratchet)

