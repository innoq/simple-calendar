(ns simple-calendar.server
  (:require  [simple-calendar.core :as core]
             [environ.core :refer [env]]
             [ring.adapter.jetty :refer [run-jetty]])
  (:gen-class))

(defn- port  []
  (Integer/parseInt  (or  (env :port) "3002")))

(defn -main  [& args]
  (core/init)
  (run-jetty
    core/webapp
    {:port  (port)
     :join? false}))
