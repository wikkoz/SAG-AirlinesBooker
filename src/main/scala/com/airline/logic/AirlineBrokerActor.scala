package com.airline.logic

import java.util.concurrent.{Executors, TimeUnit}

import akka.actor.{Actor, ActorLogging, ActorRef}
import akka.pattern.ask
import akka.pattern.pipe
import akka.util.Timeout
import com.airline.domain._
import com.airline.logic.AirlineActor.{GetAvailableTickets, ReserveTicket}
import com.airline.logic.AirlineBrokerActor.GetRequest

import scala.concurrent.{ExecutionContext, ExecutionContextExecutorService, Future}

object AirlineBrokerActor {
  final case class GetRequest(request: AirlineBrokerRequest)
}
class AirlineBrokerActor(airlineActors: Map[String, ActorRef]) extends Actor with ActorLogging {

  implicit lazy val timeout: Timeout = Timeout(5, TimeUnit.SECONDS)
  implicit val ec: ExecutionContextExecutorService = ExecutionContext.fromExecutorService(Executors.newSingleThreadExecutor)

  override def receive: Receive = {
    case GetRequest(request: AirlineBrokerRequest) =>
      val airline = airlineActors(request.airline) : ActorRef
      request.command match {
        case GetFlightsCommand() => askForAvailableTickets(airline) pipeTo sender()
        case BookTicketCommand(ticket) => orderTicket(airline, ticket) pipeTo sender()
      }

  }


  private def askForAvailableTickets(airline: ActorRef): Future[List[Flight]] = {
    (airline ? GetAvailableTickets()).mapTo[List[Flight]]
  }

  private def orderTicket(airline: ActorRef, ticket: Ticket): Future[BigDecimal] = {
    (airline ? ReserveTicket(ticket)).mapTo[BigDecimal]
  }
}
