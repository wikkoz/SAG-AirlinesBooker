package com.airline.logic

import java.time.LocalDateTime

import akka.actor.{Actor, ActorLogging, Props}
import com.airline.domain._
import com.airline.logic.AirlineActor.{GetAvailableTickets, ReserveTicket}

import scala.util.{Failure, Random, Success, Try}

object AirlineActor {

  final case class ReserveTicket(reservation: Ticket)

  final case class GetAvailableTickets()

  def props: Int => Props = (airlineName: Int) => Props(new AirlineActor(airlineName))
}

class AirlineActor(airlineName: Int) extends Actor with ActorLogging {

  private def initFlights(): List[Flight] = {
    val random = Random
    (1 to random.nextInt(10) + 1).map(i => createFlight(i)).toList
  }

  private def createFlight(id: Int): Flight = {
    val random = Random
    val cities = random.shuffle(City.values.toList)
    val departureCity = cities.head
    val destinationCity = cities(1)
    val price = random.nextInt(4) * 100 + 100
    val seats = (1 to random.nextInt(10) + 11).map(i => Seat(i, isBooked = false, BigDecimal.valueOf(price))).toList
    val date = LocalDateTime.now().plusMinutes(random.nextInt(60000))
    Flight(id, seats, departureCity, destinationCity, date)
  }

  var flights: List[Flight] = initFlights()

  override def receive: Receive = {
    case ReserveTicket(ticket: Ticket) =>
      sender() ! reserveTicket(ticket)
    case GetAvailableTickets() =>
      sender() ! flights
  }

  private def reserveTicket(ticket: Ticket): Try[BigDecimal] = {
    flights.find(f => f.flightId == ticket.flightId)
      .map(f => checkAndBookSeats(ticket, f)
      match {
        case Success((flight, price)) =>
          updateFlights(flight)
          Success(price)
        case Failure(error) => Failure(error)
      })
      .getOrElse(Failure(new IllegalArgumentException(s"Cannot find flight with id: ${ticket.flightId} ")))
  }

  private def checkAndBookSeats(ticket: Ticket, flight: Flight): Try[(Flight, BigDecimal)] = {
    val ticketSeats = for {
      seat <- ticket.seats
      flightSeat <- flight.seats.find(fs => fs.position == seat)
    } yield flightSeat

    if (ticketSeats.exists(s => s.isBooked))
      return Failure(new IllegalStateException("Not all seats are available to book"))

    val updatedSeats = flight.seats
      .map(seat => if (ticketSeats.exists(ts => ts.position == seat.position)) {
        Seat(seat.position, isBooked = true, seat.price)
      } else {
        seat
      })

    val price = ticketSeats.map(it => it.price).sum

    val updatedFlight = Flight(flight.flightId, updatedSeats, flight.departureCity, flight.destinationCity, flight.departureTime)

    Success(updatedFlight, price)
  }

  private def updateFlights(flight: Flight): Unit = {
    flights = flights.map(f => if (f.flightId == flight.flightId) flight else f)
  }
}