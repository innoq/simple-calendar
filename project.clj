(defproject simple-calendar "0.1.0-SNAPSHOT"
  :description "simple calendar app for a talk on building microservices with Clojure"
  :url "https://gitlab.innoq.com/innoq/simple-calendar"
  :dependencies [[org.clojure/clojure "1.8.0"]
                   [compojure "1.4.0"]
                   [ring "1.4.0"]
                   [ring/ring-json "0.4.0"]
                   [feedworker "0.1.0"]
                   [org.clojure/core.match "0.3.0-alpha4"]
                   [cheshire "5.5.0"]
                   [clj-http "2.1.0"]
                   [org.mnode.ical4j/ical4j "1.0.7"]
                   [com.netflix.hystrix/hystrix-clj "1.5.0"]
                   [org.clojure/tools.logging "0.3.1"]
                   [log4j "1.2.17" :exclusions [javax.mail/mail
                                                javax.jms/jms
                                                com.sun.jdmk/jmxtools
                                                com.sun.jmx/jmxri]]
                   [org.slf4j/slf4j-log4j12 "1.7.18"]
                   [environ "1.0.2"]
                   [org.clojure/core.async "0.2.374"]
                   [org.clojure/data.json "0.2.6"]
                   [org.xerial/sqlite-jdbc "3.8.11.2"]
                   [yesql "0.5.2"]
                   [ragtime "0.5.3"]]
  :plugins [[lein-ring "0.9.7"]
            [lein-environ "1.0.2"]]
  :ring {:handler simple-calendar.core/webapp
         :init simple-calendar.core/init}
  :aliases {"migrate" ["run" "-m" "db.migrate/migrate"]
            "rollback" ["run" "-m" "db.migrate/rollback"]}
  :profiles {:uberjar {:aot :all}})
