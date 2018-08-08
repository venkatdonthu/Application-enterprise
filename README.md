Requirement
===========

-Maven . [Official Install instruction](https://maven.apache.org/install.html). Linux/Mac recommended:
> brew install mvn

-Database: in memory hibernate h2.

-After that:

> cd to the folder

> mvn ninja:run

REST SERVICE END-POINT (
======================

> **localhost:8080/hotel/config** . GET/POST config. For example, with the following payload:

> {
	"rooms": 2,
	"overbookingLevel": 50
} // self explanatory

> **localhost:8080/hotel/room** . POST to create a room.

> {
	"roomNumber": 1,
	"about": "room1"
}

> **localhost:8080/hotel/reserve** . POST to reserve a room

> {
	"fullname": "Bob1",
	"email": "bob1@bob.com",
	"roomNumber": 1,
	"checkInDate": "2018-04-08",
	"checkOutDate": "2018-04-10"
} . IT WILL ALSO REPORT IF OVERBOOKING CONDITION IS MET . Sample response: **"Overbooking max has reached. Please check back later!!!!"**

> GET request return all the reservations.

> Sample response : {
    "reservations": [
        {
            "fullname": "Bob1",
            "email": "bob1@bob.com",
            "roomNumber": 1,
            "checkInDate": "2018-04-08 21:19:46",
            "checkOutDate": "2018-04-10 21:19:46"
        }
    ]
}

> **localhost:8080/hotel/insertReservation** . POST to insert data into the system. This one is different than the previous in that it does not validate checkIn and CheckOut date. Payload same as the previous



> **localhost:8080/hotel/reservations** . POST to get information on a Room at Date.

> Sample payload

> {
	"roomNumber": 1,
	"date": "2018-04-10"
}

> Sample response: 

> {
    "roomNumber": 1,
    "about": "room1",
    "available": true,
    "nextAvailable": null,
    "numberOfPeopleReserved": 0
} // This means the room is available at the request date.

** To POST data to link, either use Postmand or curl:**

> curl -H "Content-Type: application/json" -X POST -d '{"roomNumber":1,"date":"2018-04-10"}' localhost:8080/hotel/reservations

** State Change **

> localhost:8080/hotel/changeReservation/update . POST update a reservation

> {
	"action": "update",
	"fullname": "Bob",
    "email": "bob@gmail.com",
    "roomNumber": 1,
    "fromCheckInDate": "2018-04-09",
    "fromCheckOutDate":"2018-04-11",
    "toCheckInDate": "2018-04-10",
    "toCheckOutDate": "2018-04-12"
} // **change reservation from [04/09 - 04/11] -> [04/10 - 04/12]**



> localhost:8080/hotel/changeReservation/cancel . POST cancel a reservation

> {
	"action": "cancel",
	"fullname": "Bob",
    "email": "bob@gmail.com",
    "roomNumber": 1,
    "fromCheckInDate": "2018-04-09",
    "fromCheckOutDate":"2018-04-11",
    "toCheckInDate": "2018-04-10",
    "toCheckOutDate": "2018-04-12"
} // cancel reservation

Trivia
======

** Date Format ** : yyyy-MM-dd . You cannot check in/check out in the past but you can insert sample data with date in the past.

** Overbooking rate **: applies if at the date of consideration, reservations >= rooms*(1 + overbookingLevel/100)

** Rate limiter **: 1 minute within POST request of state change






