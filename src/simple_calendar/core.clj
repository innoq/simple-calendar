(ns simple-calendar.core
  (:require [compojure.core :refer :all]
            [feedworker.core :as feedworker]
            [clojure.core.match :refer [match]]
            [cheshire.core :as json]
            [clj-http.client :as client]
            [ring.middleware.json :refer [wrap-json-response wrap-json-body]]
            [simple-calendar.ical :as ical]
            [clojure.tools.logging :as log]
            [environ.core :refer [env]]
            [simple-calendar.notifications :refer [send-notification start-notifier]]
            [yesql.core :refer [defqueries]]))

(def db-spec {:connection-uri (env :database-url)})

(defqueries "db/users.sql" {:connection db-spec})
(defqueries "db/events.sql" {:connection db-spec})
(defqueries "db/participants.sql" {:connection db-spec})

(defn find-event [eventId]
  (first (db-get-event {:eventid eventId})))

(defn get-eventIds [userId]
  (map :eventid (db-get-user-events {:userid userId})))

(defn get-events [userId]
  (map find-event (get-eventIds userId)))

(defn get-user [userid]
  (first (db-get-user-by-id {:userid userid})))

(defn get-users-by-email [email]
  (db-get-user-by-email {:email email}))

(defn add-or-update-user! [userid email url]
  (if (get-user userid)
    (db-update-user! {:userid userid :email email :url url})
    (db-add-user! {:userid userid :email email :url url})))

(defn get-participant-ids [eventId]
  (map :userid (db-get-participant-ids {:eventid eventId})))

(defn add-participant! [userId eventId]
  (let [current (into #{} (get-participant-ids eventId))]
    (if-not (some current userId) (db-add-participant! {:userid userId :eventid eventId}))))

(defn new-uuid []
  (str (java.util.UUID/randomUUID)))

(defn update-contact! [url]
  (let [response (client/get url {:throw-exceptions false})]
    (match (:status response)
           200 (let [user (json/parse-string (:body response) true)
                     id (:id user)
                     email (:email user)
                     url (:self user)]
                 (add-or-update-user! id email url)
                 (log/info "Updated user" id email))
           :else nil)))

(defn contacts-feed-handler [entry worker config]
  (let [content (-> entry
                  :contents
                  first
                  :value
                  (json/parse-string true))
        type (:type content)
        payload (:payload content)
        contact-url (match type
                           "contact-updated" (:contact payload)
                           "contact-created" (:self payload)
                           :else (do
                                   (log/error "unknown event: " type " - don't know what to do")
                                   nil))]
    (update-contact! contact-url)))

(def contacts-feed (env :contacts-feed))

(def feed-config
  {:workers
   {:contacts {:url contacts-feed
               :handler contacts-feed-handler
               :processing-strategy :at-least-once
               :interval 1000}}
   :processed-entries-dir "processed-entries"
   :cleanup {:keep 10 :max 200}
   :metrics {:http {:port 8080
                    :path "/feedworker/metrics"}}})

(defn init []
  (log/info "initializing ...")
  (log/info "... feedworker")
  (feedworker/run! feed-config)
  (log/info "... starting notifier")
  (start-notifier))

(defn base-url [request]
  (str (name (:scheme request)) "://" (get (:headers request) "host")))

(defn calendar-url [userId request]
  (str (base-url request) "/calendar/" userId))

(defn user-url [userId]
  (:url (get-user userId)))

(defn event-url [eventId request]
  (str (base-url request) "/event/" eventId))

(defn participants-url [eventId request]
  (str (event-url eventId request) "/participants"))

(defn get-users-from-emails [emails]
      (map :userid (mapcat get-users-by-email emails)))

(defn add-participants! [eventId emails request]
  (let [ids (get-users-from-emails emails)
        event (find-event eventId)]
    (if-not (nil? event)
      (do
        (doseq [id ids] (add-participant! id eventId))
        (log/info "added participants " ids "to event" eventId)
        (doall (map #(send-notification % (event-url eventId request)) emails))
        true)
      false)))

(defn year? [y] (and (<= 0 y) (integer? y)))
(defn month? [m] (and (<= 1 m 12) (integer? m)))
(defn day? [d] (and (<= 1 d 31) (integer? d)))

(defn valid-event? [evt]
  (when (and (string? (:title evt))
          (year? (:year evt))
          (month? (:month evt))
          (day? (:day evt)))
    evt))

(defn store-event! [userId event request]
  (if (valid-event? event)
    (let [id (new-uuid)
          mod-event (assoc event :eventid id)]
      (db-add-event! mod-event)
      (add-participant! userId id)
      (log/info "Added event" id "with participant" userId)
      (assoc mod-event
        :event-url (event-url id request)
        :user (user-url userId)
        :calendar (calendar-url userId request)
        :participants (participants-url id request)))
    nil))

(defn get-calendar [userId request]
  (log/info :get-calendar request)
  (let [user (get-user userId)]
    (if (nil? user)
      {:status 404
       :headers {"Content-Type" "text/plain"}
       :body "No such user found"}
      {:status 200
       :headers {"Content-Type" "text/calendar"}
       :body (let [cal-events (get-events userId)
                   cal (ical/cal-to-ical
                         (calendar-url userId request)
                         cal-events)]
               (str cal))})))

(defn add-event! [userId event request]
  (log/info :add-event! request)
  (if-let [user (get-user userId)]
    (if-let [stored-event (store-event! userId event request)]
      {:status 201
       :headers {"Location" (:self stored-event)}
       :body stored-event}
      {:status 400
       :headers {"Content-Type" "text/plain"}
       :body "Could not store defined event"})
    {:status 404
     :headers {"Content-Type" "text/plain"}
     :body "No such user found"}))

(defn get-event [eventId]
  (let [event (find-event eventId)]
    (if (nil? event)
      {:status 404
       :headers {"Content-Type" "text/plain"}
       :body "No such event found"}
      {:status 200
       :headers {"Content-Type" "application/json"}
       :body event})))

(defn get-participants [eventId]
  (let [event (find-event eventId)]
    (if (nil? event)
      {:status 404}
      {:status 200
       :headers {"Content-Type" "application/json"}
       :body (map user-url (get-participant-ids eventId))})))

(defn app-routes []
  (routes
    (GET "/calendar/:userId" [userId :as request]
         (get-calendar userId request))
    (POST "/calendar/:userId" [userId :as request]
          (add-event! userId (:body request) request))
    (GET "/event/:id" [id :as request]
         (get-event id))
    (GET "/event/:id/participants" [id :as request]
         (get-participants id))
    (POST "/event/:id/participants" [id :as request]
         (if (add-participants! id (:body request) request)
           {:status 204}
           {:status 404}))))

(def webapp
  (-> (app-routes)
    wrap-json-response
    (wrap-json-body {:keywords? true})))