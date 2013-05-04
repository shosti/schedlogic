(ns schedlogic.web
  (:require [cheshire.core :refer [generate-string parse-string]]
            [compojure.route :as route]
            [schedlogic.core :refer [schedule]]
            [ring.adapter.jetty :as jetty]))

(defn schedule-tasks [tasks appts n]
  (map (fn [sched]
         (map #(hash-map :start (first %) :end (second %)) sched))
       (schedule n
                 (map #(vector (:earliest %) (:latest %) (:length %))
                      tasks)
                 (map #(vector (:start %) (:end %))
                      appts))))

(defn schedule-day [day]
  (let [{:keys [tasks appts n_schedules]} (parse-string day true)
        sorted-tasks (sort-by :id tasks)
        ids (map :id sorted-tasks)]
    (generate-string
     (map (fn [sched]
            {:tasks
             (map #(merge %1 {:id %2})
                  sched ids)})
          (schedule-tasks sorted-tasks appts n_schedules)))))

(defroutes app
  (GET "/schedule" [day]
       (schedule-day day)))
