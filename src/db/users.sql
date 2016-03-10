-- name: db-add-user!
-- adds a user with email and id
INSERT INTO Users(UserId, Email, Url) VALUES (:userid, :email, :url);

-- name: db-update-user!
-- updates existing user
UPDATE Users SET Email = :email, Url = :url WHERE UserId = :userid ;

-- name: db-get-user-by-email
SELECT UserId,Email,Url FROM Users WHERE Email = :email ;

-- name: db-get-user-by-id
SELECT UserId,Email,Url FROM Users WHERE UserId = :userid ;
