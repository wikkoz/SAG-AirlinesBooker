package com.airline.logic

import java.util.concurrent.{Executors, TimeUnit}

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.pattern.{ask, pipe}
import akka.util.Timeout
import com.airline.domain._
import com.airline.logic.AirlineActor.{GetAvailableTickets, ReserveTicket}
import com.airline.logic.AirlineBrokerActor.GetRequest

import scala.concurrent.{ExecutionContext, ExecutionContextExecutorService, Future}
import scala.util.Try

object AirlineBrokerActor {

  final case class GetRequest(request: AirlineBrokerRequest)

  def props: Map[String, ActorRef] => Props = (airlineActors: Map[String, ActorRef]) => Props(new AirlineBrokerActor(airlineActors))
}

class AirlineBrokerActor(airlineActors: Map[String, ActorRef]) extends Actor with ActorLogging {

  implicit lazy val timeout: Timeout = Timeout(5, TimeUnit.SECONDS)
  implicit val ec: ExecutionContextExecutorService = ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(10))

  override def receive: Receive = {
    case GetRequest(request: AirlineBrokerRequest) =>
      airlineActors.get(request.airline) match {
        case Some(airlineActor) =>
          request.command match {
            case GetFlightsCommand() => askForAvailableTickets(airlineActor) pipeTo sender()
            case BookTicketCommand(ticket) => orderTicket(airlineActor, ticket) pipeTo sender()
          }
        case None =>  Future.failed(new RuntimeException("Airline not found")) pipeTo sender()
      }
  }

  private def askForAvailableTickets(airline: ActorRef): Future[List[Flight]] = {
    (airline ? GetAvailableTickets()).mapTo[List[Flight]]
  }

  private def orderTicket(airline: ActorRef, ticket: Ticket): Future[Try[BigDecimal]] = {
    (airline ? ReserveTicket(ticket)).mapTo[Try[BigDecimal]]
  }
}
