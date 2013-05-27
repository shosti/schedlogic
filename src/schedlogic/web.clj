;; ### The SchedLogic API

;; The API is a thin wrapper around the `schedule` function. There is
;; a single endpoint, `"/schedule"`, which takes a single JSON
;; parameter, `day`, and returns a JSON response. The request JSON
;; follows this format:
;;
;;     {tasks: [{earliest: e, latest: l, id: id}*],
;;      appts: [{start: s, end: e}*],
;;      n_schedules: n}
;;
;; The response JSON is an array at most `n` schedules of form
;; `[{start: s, end: e, id: id}*]` (i.e. a list of scheduled tasks).
;; Task ids are preserved, although order might not be.
;;
;; If no schedules are possible with the given parameters, the
;; response will be a JSON string of `"none"`. If scheduling takes too
;; long or there is an error, the response will be `"failure"`.

(ns schedlogic.web
  (:require [cheshire.core :refer [generate-string parse-string]]
            [compojure.core :refer [defroutes GET]]
            [compojure.handler :as handler]
            [schedlogic.core :refer [schedule]]
            [ring.adapter.jetty :refer [run-jetty]]))

(def time-limit
  "The time limit for scheduling is currently hard coded at 10 seconds."
  10000)

(defn schedule-tasks
  "The request data is mapped to and from the appropriate form for the
`schedule` function."
  [tasks appts n]
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
              (map #(merge %1 {:id %2})
                   sched ids))
            schedules)))))

(defn schedule-day
  "The `schedule` function runs in a separate thread. If it has not
finished when the time limit is passed, `\"failure\"` is returned."
  [day]
  (let [a (agent day)]
    (send a attempt-schedule-day)
    (if (await-for time-limit a)
      @a
      (generate-string "failure"))))

;; There is a single API endpoint, `"/schedule"`.

(defroutes app-routes
  (GET "/schedule" [day]
       (schedule-day day)))

(def app
  (handler/site app-routes))

(defn -main
  "The app is served by Jetty."
  [& [port]]
  (let [port (Integer. (or (System/getenv "PORT") 5000))]
    (run-jetty app {:port port})))
