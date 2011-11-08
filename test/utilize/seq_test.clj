(ns utilize.seq-test
  (:use clojure.test utilize.seq clojure.set midje.sweet))

(deftest test-zip
  (is (= [[1 4 8] [2 5 9] [3 6 nil] [nil 7 nil]] (zip [1 2 3] [4 5 6 7] [8 9]))))

(deftest test-insert
  (is (= [1 2 3 4 5] (insert [2 3] 1 [1 4 5]))))

(deftest test-find-with
  (is (= :foo (find-with odd?  [2 4 5 7] [:bar :baz :foo :bap])))
  (is (= nil  (find-with even? [1 3 5 9] [:bar :baz :foo :bap]))))

(deftest test-cross
  (is (= '((0 0) (0 1) (1 0) (1 1))          (cross [0 1] [0 1])))
  (is (= '((0 0 2) (0 1 2) (1 0 2) (1 1 2))) (cross [0 1] [0 1] [2])))

(deftest test-lazy-cross
  (is (= '((0 0) (1 0) (0 1) (1 1))         (lazy-cross [0 1] [0 1])))
  (is (= '((0 0 2) (1 0 2) (0 1 2) (1 1 2)) (lazy-cross [0 1] [0 1] [2]))))

(deftest test-extract
  (is (= [5 '(2 4 6 1 2 7)] (extract odd?     [2 4 6 5 1 2 7])))
  (is (= [2 '(4 6 5 1 2 7)] (extract even?    [2 4 6 5 1 2 7])))
  (is (= [7 '(2 4 6 5 1 2)] (extract #(< 6 %) [2 4 6 5 1 2 7]))))

(deftest test-separate
  (is (= ['(5 1 7) '(2 4 6 2)] (separate odd?  [2 4 6 5 1 2 7])))
  (is (= ['(2 4 6 2) '(5 1 7)] (separate even? [2 4 6 5 1 2 7]))))

(deftest test-partition-between
  (let [input [1 nil nil 2 3 nil 4]]
    (are [f output] (= output (partition-between f input))
         (fn [[a b]] (not (nil? a)))           [[1] [nil nil 2] [3] [nil 4]],
         (fn [[a b]] (not (nil? b)))           [[1 nil nil] [2] [3 nil] [4]],
         (partial some nil?)                   [[1] [nil] [nil] [2 3] [nil] [4]],
         (fn [[a b]] (not= (nil? a) (nil? b))) [[1] [nil nil] [2 3] [nil] [4]])))

(deftest test-include?
  (is (include? 5 [1 2 3 4 5]))
  (is (include? :bar '(1 4 :bar)))
  (is (not (include? 2 '(1 3 4))))
  (is (not (include? :foo [1 :bar :baz 3]))))

(deftest test-unfold
  (is (= [0 1 1 2 3 5 8 13 21 34]
         (take 10 (unfold (fn [[a b]]
                            [a [b (+ a b)]])
                          [0 1])))))

(deftest test-take-shuffled
  (let [nums (set (range 10))]
    (is (= nums (set (take-shuffled (count nums) nums))))
    (is (= 5 (count (take-shuffled 5 nums))))
    (is (subset? (set (take-shuffled 3 nums)) nums))))

(deftest test-find-first
  (is (= 5 (find-first odd? [2 5 9])))
  (is (nil? (find-first (constantly false) (range 1000)))))

(deftest test-lazy-loop
  (is (= (range 10)
         (lazy-loop [i 0]
           (when-not (= i 10)
             (cons i (lazy-recur (inc i)))))))
  (testing "0-arg lazy-loop"
    (is (= [1 1 1] (take 3
                         (lazy-loop []
                           (cons 1 (lazy-recur))))))))

(deftest test-alternates
  (is (= '[[a b] [1 2]]
         (alternates '[a 1 b 2])))
  (is (= '[[0 3 6] [1 4 7] [2 5 8]]
         (alternates 3 (range 9))))
  (testing "Doesn't blow up for empty seqs"
    (let [a (alternates [])]
      (testing "Lazy if nothing forced."
        (is a))
      (is (not (seq a))))))

(deftest test-slice
  (let [size 900, slices 7, coll (range size),
        sliced (slice slices coll), largest (apply max (map count sliced))]
    (testing "We get all the items back in order"
      (is (= coll (apply concat sliced))))
    (testing "We get the right number of slices"
      (is (= slices (count sliced))))
    (testing "Slices are sized regularly"
      (is (every? #(<= (Math/abs (- % largest)) 1)
                 (map count sliced))))))

(deftest test-foldr
  (is (= [1 2 3 4]
         (foldr cons nil [1 2 3 4]))))

(deftest test-lazy
  (let [realized (atom 0)
        realize (fn [x] (swap! realized inc) x)
        the-list (lazy (realize 1) (realize 2))]
    (is (= 0 @realized))
    (is (= 1 (first the-list)))
    (is (= 1 @realized))
    (is (= 2 (second the-list)))
    (is (= 2 @realized))
    (is (nil? (next (next the-list))))
    (is (= 2 @realized))))

(fact "use size-1 laziness, instead of 32-piece chunks"
  (first (map prn (unchunk (range)))) => anything
  (provided
    (prn anything) => anything :times 1)) ;; without unchunk this would be 32

(tabular
  (fact "returns the first function that is truthy for the given args"
    (first-truthy-fn ?fns :kw)  => (exactly ?first-truthy-fn))

     ?fns                ?first-truthy-fn
     [string? keyword?]  keyword?
     [string? nil?]      nil
     []                  nil
     [keyword? odd?]     keyword? ) ;; shortcircuits when it reaches a match - evaluating (odd? :kw) would have blown up

(tabular
  (fact "version of interleave that also works for 0 or 1 sequences"
    (apply interleave++ ?seqs) => ?interleaved)

  ?seqs                ?interleaved
  []                   []
  [[1 2 3]]            [1 2 3]
  [[1 2 3] [44 55 66]] [1 44 2 55 3 66])

(fact "throws when no items"
  (only []) => (throws RuntimeException "should have precisely one item, but had 0")
  (only [1]) => 1
  (only [1 2]) => (throws RuntimeException "should have precisely one item, but had at least 2")
  (only (repeat 5)) => (throws RuntimeException "should have precisely one item, but had at least 2"))
