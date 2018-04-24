package org.example.reactor

import java.time.Duration
import java.time.Instant

data class User(val id: String, val name: String)

data class Reservation(val id: String, val time: Instant, val userId: String, val location: Location)

data class Location(val x: Float, val y: Float)

data class Ride(val reservation: Reservation, val vehicle: Vehicle)

data class Vehicle(val id: String)

data class RideOffer(val vehicle: Vehicle, val time: Instant, val location: Location)

data class RideBid(val arrivalDelay: Duration, val vehicle: Vehicle, val reservationId: String)