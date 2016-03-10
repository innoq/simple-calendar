# simple-calendar

This app is an example application to show how microservices can be developed using Clojure.

## Setup

### Start the [simple-contacts app](https://github.com/innoq/simple-contacts) 

The _simple-calendar_ app depends on the _simple-contacts_ app which adds contacts and publishes their information via atom feed. 
After starting the application, the atom feed will then be available at some URL (for instance http://localhost:3000/feed)

For test purposes, a contact can be added via the following HTTP request (if the _simple-contacts_ app is running on http://localhost:3000/):

    curl -i http://localhost:3000/contacts -XPOST --data '{"firstname" : "mr", "lastname" : "froggy", "email" : "mr.froggy@puddle.com"}' -H'Content-Type: application/json'
    
### Start a notifications service (optional)

Currently, when participants are added to an event, they should be notified.
In this app, notifications will be sent out via post request to a specified notification server running at a user specified URL (i.e. http://localhost:3003).
The notifications have the form ```{:email "mr.frog@example.com" :message "Hi!"}``` and the expectation is that the specified service will send out the email.

**NOTE:** The implementation for the notification service was intended to show how the application uses [hysterix](https://github.com/Netflix/Hystrix) as a circuit breaker to handle services which are not currently available. For this reason, we have not developed the notification app itself, although a service like [naveed](https://github.com/innoq/naveed) or [Amazon SES](https://aws.amazon.com/ses/) could be used for this purpose.
If there is no service specified, the app will not attempt to send out notifications.

### Define the dependencies via environmental variables (for instance in profiles.clj)

We developed the application following the [12 Factor App](http://12factor.net/) pattern. For this reason, we define the dependencies via environment variables (using the [environ](https://github.com/weavejester/environ) library). The atom feed for the contacts app must be saved in the environment variable `CONTACTS_FEED`. The URL for the notification service can be specified using the environment variable `NOTIFICATION_SERVICE`. 

The URL for the backing database is also specified with the environment variable `DATABASE_URL`. The database drivers for SQLite are included in the project.

These environmental variables can also be specified for the project with a `profiles.clj` file in the project root with the following content:

```clojure
{:dev  {:env {:database-url "jdbc:sqlite:db/app.db"
              :contacts-feed "http://localhost:3000/feed"
              :notification-service "http://localhost:3003"}}}
```

### Setup Database

If the database url has been properly defined in the last step, the database can be initialized with:

    lein migrate

### Start Application

The app can then be started with:

    lein ring server-headless 3002

### Running the Application

After adding a contact within the [simple-contacts app](https://github.com/innoq/simple-contacts), a response will be sent containing the user id for the contact which has been added.

The app supports the following:

1. Retrieving a calendar for a user
		
		curl -i http://localhost:3002/calendar/<userId>
		
2. Adding an event to a calendar (currently only full day events are supported). The response will include the generated id for this event
		
		curl -i http://localhost:3002/calendar/<userId> -XPOST --data '{"title": "Test", "day": 30, "month": 3, "year": 2015}' -H'Content-Type: application/json'
	
3. The event information can then be retrieved with:

		curl -i http://localhost:3002/event/<eventId>
		
4. The participant list can be retrieved with:

		curl -i http://localhost:3002/event/<eventId>/participants

5. Participants can also be added to the event. Here the data of the post request is a list of emails. It is assumed that users with these email addresses have already been added to the [simple-contacts app](https://github.com/innoq/simple-contacts). If a notification service has been defined, the app will attempt to send an email notification to the specified users.

		curl -i http://localhost:3002/event/<eventId>/participants -XPOST --data '["mr.kitty@litterbox.com","mr.piggy@muddy.com"]' -H'Content-Type: application/json'


## Author information and license

Copyright 2015 innoQ Deutschland GmbH. Published under the Apache 2.0 license.
