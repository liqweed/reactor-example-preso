package org.example.reactor

import java.time.Instant

data class ReservationDTO(val id: String, val time: Instant, val userId: String, val location: Location)
