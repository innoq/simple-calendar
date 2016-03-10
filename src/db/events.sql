-- name: db-add-event!
INSERT INTO Events(EventId, Title, Year, Month, Day) VALUES (:eventid, :title, :year, :month, :day) ;

-- name: db-get-event
SELECT Title,Year,Month,Day FROM Events WHERE EventId = :eventid ;