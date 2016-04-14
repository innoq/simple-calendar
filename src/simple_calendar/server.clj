(ns simple-calendar.server
  (:require [db.migrate :as db]
            [environ.core :refer [env]]
            [ring.adapter.jetty :refer [run-jetty]]
            [simple-calendar.core :as core])
  (:gen-class))

(defn- port  []
  (Integer/parseInt  (or  (env :port) "3002")))

(defn -main  [& args]
  (db/migrate)
  (core/init)
  (run-jetty
    core/webapp
    {:port  (port)
     :join? false}))
