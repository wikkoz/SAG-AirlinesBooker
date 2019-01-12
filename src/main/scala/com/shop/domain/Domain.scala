package com.shop.domain

import java.time.LocalDateTime


final case class Ticket(seats: List[Int], flightId: Int)

final case class Seat(position: Int, isBooked: Boolean, price:BigDecimal)

final case class Flight(flightId: Int, seats: List[Seat], departureCity: City.Value, destinationCity: City.Value, departureTime: LocalDateTime)

object City extends Enumeration {
  val Warsaw, London, Berlin, Paris, Chicago, Tokyo, Moscow = Value
}