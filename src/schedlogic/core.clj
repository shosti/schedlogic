;; ### The SchedLogic Scheduling Algorithm

;; SchedLogic schedules tasks within a day given a set of fixed
;; appointments and task constraints. The constraints are as follows:
;; each task has an earliest time, a latest time, and a length.
;; Appointments are encoded by start and end times. Tasks can be
;; scheduled at any time within their given timeframe, but no tasks or
;; appointments can overlap. In order to keep the search space
;; manageable, all times are encoded as 15-minute intervals within a
;; day (time intervals are inclusive—for example, `[0 4]` would be a
;; period of 1 hour starting at the beginning of the day). The
;; approach uses finite-domain constraint logic programming, provided
;; by the `clojure.core.logic` library.

(ns schedlogic.core
  (:refer-clojure :exclude [==])
  (:require [clojure.core.logic    :refer [==
                                           conde
                                           everyg
                                           fresh
                                           lvars
                                           run run*]]
            [clojure.core.logic.fd :refer [eq
                                           in
                                           interval
                                           difference]]))

(def day-intervals
  "There are 96 intervals of 15 minutes within a day."
  (* 24 4))

(def unit-interval -1)

(defn- zip [xs ys]
  (map vector xs ys))

(defn distinct-intervals
  "No two time intervals can overlap."
  [[[lb1 ub1] [lb2 ub2]]]
  (conde
   [(eq (<= ub1 lb2))]
   [(eq (<= ub2 lb1))]))

(defn constrain-task
  "Each task must start on or after its `earliest` time, end on or
  before its `latest` time, and have a length of `length`."
  [[[start end] [earliest latest length]]]
  (eq
   (= (- end start) length)
   (>= start earliest)
   (<= end latest)))

(defn create-domain
  "The domain in which tasks can be scheduled is defined as the
  difference between the domain of the day (with 96 intervals) and the
  domains of the fixed appointments. Appointment domains are shrunk
  inward to allow the possibility of shared start/end times between
  tasks.

  This constraint is not strictly necessary, but helps cut down the
  search space."
  [appts]
  (reduce difference (interval day-intervals)
          (map (fn [[start end]]
                 (if (<= (- end start) 1)
                   unit-interval
                   (interval (inc start) (dec end))))
               appts)))

(defn schedule
  "The scheduling problem is specified as follows:

Given a vector of `[earliest latest length]` tasks and `[start end]`
appointments, find the first `n` schedules (a vector of `[start end]` scheduled tasks) such that:

1. Each task start and end time is within the domain specified by
`create-domain`;
2. Each task start and end time is constrained according to `constrain-task`;
3. No two tasks or appointments overlap.

The `run` function of `clojure.core.logic` takes care of actually
  solving the constraints."
  [n tasks appts]
  (let [task-times (zip (lvars (count tasks)) (lvars (count tasks)))
        task-domain (create-domain appts)]
    (run n [q]
      (== q task-times)
      (everyg #(in (% 0) (% 1) task-domain) task-times)     ; (1)
      (everyg constrain-task (zip task-times tasks))        ; (2)
      (let [tasks-and-appts (concat task-times appts)]
        (everyg distinct-intervals (for [t1 tasks-and-appts ; (3)
                                         t2 tasks-and-appts
                                         :when (not (= t1 t2))]
                                     [t1 t2]))))))
