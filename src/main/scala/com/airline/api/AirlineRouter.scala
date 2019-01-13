package com.airline.api

import akka.actor.ActorRef
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives.{complete, _}
import akka.http.scaladsl.server.Route
import com.airline.domain.{AirlineBrokerRequest, BookTicketCommand, Ticket}
import com.airline.logic.AirlineBrokerActor.GetRequest

import scala.util.{Failure, Success, Try}

trait AirlineRouter extends JsonSupport {
  def airlineBrokers: Map[Int, ActorRef]

  val airlineRoutes: Route =
    pathPrefix(IntNumber) {
      brokerId => {
        pathPrefix(Segment) {
          lineId => {
            path("book") {
              post {
                entity(as[Ticket]) { ticket =>
                  airlineBrokers.get(brokerId)
                    .map(brokerRef => Try(brokerRef.tell(GetRequest(AirlineBrokerRequest(lineId, BookTicketCommand(ticket))), brokerRef)))
                  match {
                    case Some(result) => result match {
                      case Success(_) => complete(StatusCodes.Created)
                      case Failure(_) => complete(StatusCodes.InternalServerError)
                    }
                    case None => complete(StatusCodes.NotFound)
                  }
                }
              }
            }
          }
        }
      }
    }
}