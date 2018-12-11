package com.database

import java.time.LocalDateTime
import scala.language.postfixOps

trait WithId {
  def id: Long
}

final case class ReservationEntity(id: Long, timestamp: LocalDateTime, seats: List[SeatsEntity]) extends WithId

final case class SeatsEntity(id: Long, isBooked: Boolean) extends WithId

final case class FlightEntity(id: Long, seats: List[SeatsEntity]) extends WithId


class Database(
                private var seatsEntities: List[SeatsEntity] = (1 to 10).map(id => SeatsEntity(id, isBooked = false)) toList,
                private var flightEntitities: List[FlightEntity] = List(FlightEntity(1, (1 to 10).map(id => SeatsEntity(id, isBooked = false)) toList)),
                private var reservationEntities: List[ReservationEntity] = List[ReservationEntity]()

              ) {
  def bookSeat(seatId: Long) = {
    val seat = seatsEntities.find(_.id == seatId).get
    SeatsEntity(seat.id, true)
  }

  def createReservation(seats: List[SeatsEntity]): ReservationEntity = {
    val seatsBooked = seats.map(seat => bookSeat(seat.id))
    ReservationEntity(findNextDatabaseId(reservationEntities), LocalDateTime.now(), seatsBooked)
  }

  def findReservation(reservationId: Long): Unit = {
    reservationEntities.find(_.id == reservationId).get
  }

  def findFlight(flightId: Long): Unit = {
    flightEntitities.find(_.id == flightId).get
  }

  private def findNextDatabaseId(list: List[WithId]) =
    if (list.isEmpty) 0L else list.maxBy(_.id).id + 1
}
