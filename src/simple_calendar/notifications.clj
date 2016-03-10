(ns simple-calendar.notifications
  (:require [com.netflix.hystrix.core :as hystrix]
            [clojure.core.async :refer [<! >! chan go go-loop]]
            [clojure.tools.logging :as log]
            [clj-http.client :as client]
            [clojure.data.json :as json]
            [environ.core :refer [env]]))

(def notifications (chan))

(defn send-notification [email event-link] 
  (log/info "add notification task to queue for" email)
  (go (>! notifications {:email email
                         :event-link event-link})))

(defn notify-fallback [email event-link]
  (let [isOpen (.isCircuitBreakerOpen hystrix/*command*)]
    (send-notification email event-link)
    (not isOpen)))

(def notification-service (env :notification-service))

; returns false if circuit-breaker is open
(hystrix/defcommand notify-user
  {:hystrix/fallback-fn notify-fallback}
  [email event-link]
  (log/info "start sending notification to" email)
  (when notification-service
    (client/post notification-service
      {:content-type :json
       :body (json/write-str
               {:email email
                :message (str "You are invited to " event-link)})}))
  (log/info "sent notification to" email)
  true)

(defn start-notifier []
  (go-loop [message (<! notifications)]
           (if-not (notify-user (:email message) (:event-link message))
             (do
               (log/error "Cannot reach notification service - will wait until next try")
               (Thread/sleep 5000)))
           (recur (<! notifications))))