package com.example.campusconnect
data class EventModel(
    var eventName: String? = null,
    var eventDate: String? = null,
    var eventTime: String? = null,
    var eventLocation: String? = null,
    var eventOrganizer: String? = null,
    var eventType: String? = null,
    var eventCapacity: String? = null,
    var eventDescription: String? = null,
    var eventFlyer: String? = null,
    var eventIcon: String? = null,
    var eventTemp:Float?=null,
    var eventHum:Float?=null,
    var eventWarning:Int?=null,
    var eventId: String? = null

    )
