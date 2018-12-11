package com.shop.domain

import java.time.LocalDateTime


final case class Reservation(id: Long, timestamp: LocalDateTime, seats: List[Seats])

final case class Seats(id: Long, isBooked: Boolean)

final case class Flight(id: Long, seats: List[Seats])