package com.database

import com.shop.domain
import com.shop.domain.{Flight, Reservation, Seats}

trait DatabaseEntitiesMapper {

	def reservationEntityToReservation(reservationEntity: ReservationEntity): Reservation = {
		domain.Reservation(reservationEntity.id, reservationEntity.timestamp, reservationEntity.seats.map(seatsEntityToSeats))
	}

	def seatsEntityToSeats(seatsEntity: SeatsEntity): Seats = {
		Seats(seatsEntity.id, seatsEntity.isBooked)
	}

	def flightEntityToFlight(flightEntity: FlightEntity): Flight = {
		domain.Flight(flightEntity.id, flightEntity.seats.map(seatsEntityToSeats))
	}
}
