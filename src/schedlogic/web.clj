(ns schedlogic.web
  (:require [cheshire.core :refer [generate-string parse-string]]
            [compojure.core :refer [defroutes GET]]
            [compojure.handler :as handler]
            [schedlogic.core :refer [schedule]]
            [ring.adapter.jetty :refer [run-jetty]]))

(def ^:dynamic time-limit 10000)

(defn schedule-tasks [tasks appts n]
  (map (fn [sched]
         (map #(hash-map :start (first %) :end (second %)) sched))
       (schedule n
                 (map #(vector (:earliest %) (:latest %) (:length %))
                      tasks)
                 (map #(vector (:start %) (:end %))
                      appts))))

(defn attempt-schedule-day [day]
  (let [{:keys [tasks appts n_schedules]} (parse-string day true)
        sorted-tasks (sort-by :id tasks)
        ids (map :id sorted-tasks)
        schedules (schedule-tasks sorted-tasks appts n_schedules)]
    (generate-string
     (if (or (empty? schedules) (every? empty? schedules))
       "none"
       (map (fn [sched]
              {:tasks
               (map #(merge %1 {:id %2})
                    sched ids)})
            schedules)))))

(defn schedule-day [day]
  (let [a (agent day)]
    (send a attempt-schedule-day)
    (if (await-for time-limit a)
      @a
      (generate-string "failure"))))

(defroutes app-routes
  (GET "/schedule" [day]
       (schedule-day day)))

(def app
  (handler/site app-routes))

(defn -main [& [port]]
  (let [port (Integer. (or (System/getenv "PORT") 5000))]
    (run-jetty app {:port port})))
