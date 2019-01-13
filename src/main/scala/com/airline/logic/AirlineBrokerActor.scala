package com.airline.logic

import java.util.concurrent.{Executors, TimeUnit}

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.pattern.{ask, pipe}
import akka.util.Timeout
import com.airline.domain._
import com.airline.logic.AirlineActor.{GetAvailableTickets, ReserveTicket}
import com.airline.logic.AirlineBrokerActor.GetRequest

import scala.concurrent.{ExecutionContext, ExecutionContextExecutorService, Future}

object AirlineBrokerActor {

  final case class GetRequest(request: AirlineBrokerRequest)

  def props: Props = Props(new AirlineBrokerActor(Map.empty))
}

class AirlineBrokerActor(airlineActors: Map[String, ActorRef]) extends Actor with ActorLogging {

  implicit lazy val timeout: Timeout = Timeout(5, TimeUnit.SECONDS)
  implicit val ec: ExecutionContextExecutorService = ExecutionContext.fromExecutorService(Executors.newSingleThreadExecutor)

  override def receive: Receive = {
    case GetRequest(request: AirlineBrokerRequest) =>
      airlineActors.get(request.airline) match {
        case Some(airlineActor) =>
          request.command match {
            case GetFlightsCommand() => askForAvailableTickets(airlineActor) pipeTo sender()
            case BookTicketCommand(ticket) => orderTicket(airlineActor, ticket) pipeTo sender()
          }
        case None => sender ! new RuntimeException("Airline not found")
      }

      context.stop(self);
  }

  private def askForAvailableTickets(airline: ActorRef): Future[List[Flight]] = {
    (airline ? GetAvailableTickets()).mapTo[List[Flight]]
  }

  private def orderTicket(airline: ActorRef, ticket: Ticket): Future[BigDecimal] = {
    (airline ? ReserveTicket(ticket)).mapTo[BigDecimal]
  }
}
