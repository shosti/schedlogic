(defproject schedlogic "0.1.0"
  :description "A tiny web service that schedules tasks within a day."
  :url "http://schedlogic.herokuapp.com"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/core.logic "0.8.3"]
                 [cheshire "5.1.1"]
                 [compojure "1.1.5"]
                 [ring "1.1.8"]]
  :min-lein-version "2.0.0")
