(ns cdt.test.core
  (:use [flock.birds] :reload)
  (:use [clojure.test]))

(defn small-world
  []
  (create-world 3))

(defn create-world-with-birds
  []
  (let [sw (small-world)]
    (create-bird [0 0] 2 sw)
    (create-bird [1 0] 2 sw)
    (create-bird [1 1] 0 sw)
    sw))

(deftest birds-dir-in-view-test
  (let [sw (create-world-with-birds)
        bird (:bird @(place [0 0] sw))
        no-bird (:bird @(place [2 0] sw))]
    (is bird)
    (is (= (:dir bird) 2))
    (is (not no-bird))))

(deftest average-from-dirs-test
  (is (= 2 (most-common-dir '(2 2))))
  (is (= 1 (most-common-dir '(1 7 1))))
  (is (some #{1 7} [(most-common-dir '(7 1))]))
  (is (some #{2 8} [(most-common-dir '(8 2))]))
  (is (= 1 (most-common-dir '(7 1))))
  (is (nil? (most-common-dir '())))
  (is (= 1 (most-common-dir '(1 5 1)))))



