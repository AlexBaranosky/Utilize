(ns ^{:dont-test "Just aliases for other functions/macros"}
  utilize.experimental.unicode
  (:use [utilize.utils :only [map-entry]]
        [utilize.macro :only [defalias macro-do]]))

(macro-do [dest src]
  `(defalias ~dest ~src)
  ∮ map-entry
  ! complement
  ∘ comp
  φ partial)
