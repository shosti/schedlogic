(ns schedlogic.core-test
  (:require [clojure.test :refer :all]
            [schedlogic.core :refer :all]))

(deftest util
  (testing "Zip"
    (is (= (zip [:a :b :c] [1 2 3])
           [[:a 1] [:b 2] [:c 3]]))))

(deftest test-schedule
  (testing "scheduling a day with one possibility"
    (is (= (set (schedule 5 [[0 4 2] [0 4 2]] []))
           #{(list [0 2] [2 4]) (list [2 4] [0 2])})))

  (testing "scheduling an impossible day"
    (is (= (schedule 3 [[0 4 2] [0 4 3]] [])
           '()))))
