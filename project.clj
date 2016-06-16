(defproject simple-calendar "0.1.0-SNAPSHOT"
  :dependencies [[cheshire "5.6.1"]
                 [clj-http "3.0.0"]
                 [compojure "1.5.0"]
                 [com.netflix.hystrix/hystrix-clj "1.5.2"]
                 [environ "1.0.2"]
                 [feedworker "0.1.0"]
                 [log4j "1.2.17" :exclusions [javax.mail/mail
                                              javax.jms/jms
                                              com.sun.jdmk/jmxtools
                                              com.sun.jmx/jmxri]]
                 [org.clojure/clojure "1.8.0"]
                 [org.clojure/core.async "0.2.374"]
                 [org.clojure/core.match "0.3.0-alpha4"]
                 [org.clojure/data.json "0.2.6"]
                 [org.clojure/tools.logging "0.3.1"]
                 [org.mnode.ical4j/ical4j "1.0.7"]
                 [org.slf4j/slf4j-log4j12 "1.7.21"]
                 [org.xerial/sqlite-jdbc "3.8.11.2"]
                 [ragtime "0.5.3"]
                 [ring/ring-core "1.4.0"]
                 [ring/ring-jetty-adapter "1.4.0"]
                 [ring/ring-json "0.4.0"]
                 [yesql "0.5.2"]]
  :min-lein-version "2.0.0"
  :profiles {:uberjar {:aot [simple-calendar.server]}}
  :plugins  [[lein-ring "0.9.7"]
             [lein-environ "1.0.2"]]
  :ring {:handler simple-calendar.core/webapp
         :init simple-calendar.core/init}
  :main simple-calendar.server
  :uberjar-name "simple-calendar.jar"
  :aliases {"migrate" ["run" "-m" "db.migrate/migrate"]
            "rollback" ["run" "-m" "db.migrate/rollback"]})
