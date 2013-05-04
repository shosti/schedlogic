(ns schedlogic.web-test
  (:require [clojure.test :refer :all]
            [schedlogic.web :refer :all]
            [cheshire.core :refer :all]))

(def sample-tasks
  [{:earliest 0
    :latest 2
    :length 2
    :id 3}
   {:earliest 3
    :latest 4
    :length 1
    :id 1}])

(def sample-appts
  [{:start 2
    :end 3}])

(def sample-day
  (generate-string
   {:tasks sample-tasks
    :appts sample-appts
    :n_schedules 1}))

(def impossible-tasks
  [{:earliest 0 :latest 2 :length 3 :id 1}])

(def impossible-day
  (generate-string
   {:tasks impossible-tasks
    :appts []
    :n_schedules 1}))

(def sample-response-tasks
  [{:start 0
    :end 2}
   {:start 3
    :end 4}])

(def sample-response
  (generate-string
   [{:tasks [{:start 3
              :end 4
              :id 1}
             {:start 0
              :end 2
              :id 3}]}]))

(deftest test-schedule-day
  (testing "schedule tasks"
    (is (= (first (schedule-tasks sample-tasks sample-appts 1))
           sample-response-tasks)))

  (testing "Schedule a day"
    (is (= (schedule-day sample-day)
           sample-response)))

  (testing "Schedule an impossible day"
    (is (= (schedule-day impossible-day)
           (generate-string "none")))))
