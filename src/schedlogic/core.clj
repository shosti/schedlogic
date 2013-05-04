(ns schedlogic.core
  (:refer-clojure :exclude [==])
  (:require [clojure.core.logic    :refer [==
                                           conde
                                           everyg
                                           fresh
                                           lvars
                                           run]]
            [clojure.core.logic.fd :refer [eq
                                           in
                                           interval]]))

(def day-intervals (* 24 4))

(defn- zip [xs ys]
  (map vector xs ys))

(defn distinct-intervals [[[lb1 ub1] [lb2 ub2]]]
  (conde
   [(eq (<= ub1 lb2))]
   [(eq (<= ub2 lb1))]))

(defn constrain-task [[[start end] [earliest latest length]]]
  (eq
   (= (- end start) length)
   (>= start earliest)
   (<= end latest)))

(defn task-domain [[start end]]
  (in start end (interval day-intervals)))

(defn schedule [n tasks appts]
  "Find the first n ways to schedule tasks given set appts. Tasks
should be a vector of [earliest latest length] vectors, and appts
should be a vector of [start end] vectors. All times are represented
as integers between 0 and 96, representing 15-minute intervals in a
day."
  (let [task-times (zip (lvars (count tasks)) (lvars (count tasks)))]
    (run n [q]
      (== q task-times)
      (everyg task-domain task-times)
      (everyg constrain-task (zip task-times tasks))
      (let [tasks-and-appts (concat task-times appts)]
        (everyg distinct-intervals (for [t1 tasks-and-appts
                                         t2 tasks-and-appts
                                         :when (not (= t1 t2))]
                                     [t1 t2]))))))
