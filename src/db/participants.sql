-- name: db-add-participant!
INSERT INTO Participants(EventId,UserId) values(:eventid,:userid);

-- name: db-get-participant-ids
SELECT UserId FROM Participants WHERE EventId = :eventid;

-- name: db-get-user-events
SELECT EventId FROM Participants WHERE UserId = :userid;