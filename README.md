# simple-calendar

simple calendar app for a talk on building microservices with Clojure

## Start the [contacts app](https://gitlab.innoq.com/innoq/contacts) 

This will then be available at some URL (for instance http://localhost:3000)

## Define the dependencies via environmental variables (for instance in profiles.clj)

```clojure
{:dev  {:env {:database-url "jdbc:sqlite:db/app.db"
              :contacts-feed "http://localhost:3000/feed"
              :notification-service "http://localhost:3003"}}}
```

## Setup Database

    lein migrate

## Start Application

    lein ring server-headless 3002

## Example

For now only full day events are supported. The userId must exist in the [contacts app](https://gitlab.innoq.com/innoq/contacts)

    POST http://localhost:3002/calendar/<userId>
    Content-Type: application/json

    {"title": "Test", "day": 30, "month": 3, "year": 2015}


## Author information and license

Copyright 2015 innoQ Deutschland GmbH. Published under the Apache 2.0 license.
