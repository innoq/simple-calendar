(ns simple-calendar.ical
  (:import [net.fortuna.ical4j.model Calendar Date]
           [net.fortuna.ical4j.model.property ProdId CalScale Version Uid]
           [net.fortuna.ical4j.model.component VEvent]))

(defn date-to-time [year month day]
  (let [cal (java.util.Calendar/getInstance)]
    (.set cal java.util.Calendar/YEAR year)
    (.set cal java.util.Calendar/MONTH (dec month))
    (.set cal java.util.Calendar/DAY_OF_MONTH day)
    cal))

(defn event-to-ical [event]
  (let [day (:day event)
        month (:month event)
        year (:year event)
        summary (:title event)
        id (:id event)
        cal (date-to-time year month day)
        date (Date. (.getTime cal))
        cal-event (VEvent. date date summary)
        props (.getProperties cal-event)]
    (.add props (Uid. id)) 
    cal-event))

(defn cal-to-ical [name events]
             (let [cal (Calendar.)
                   props (.getProperties cal)
                   cal-events (map event-to-ical events)
                   components (.getComponents cal)]
               (.add props Version/VERSION_2_0)
               (.add props (ProdId. name))
               (.add props CalScale/GREGORIAN)
               (doall (map #(.add components %) cal-events))
               cal))



