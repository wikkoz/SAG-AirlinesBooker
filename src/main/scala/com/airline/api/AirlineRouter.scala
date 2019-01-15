package com.airline.api

import java.util.concurrent.{Executors, TimeUnit}

import akka.actor.ActorRef
import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Directives.{complete, _}
import akka.http.scaladsl.server.Route
import akka.pattern.ask
import akka.util.Timeout
import com.airline.domain._
import com.airline.logic.AirlineBrokerActor.GetRequest
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule

import scala.concurrent.{ExecutionContext, ExecutionContextExecutorService, Future}
import scala.util.{Failure, Success, Try}

trait AirlineRouter extends JsonSupport {
  def airlineBrokers: Map[Int, ActorRef]
  def objectMapper = new ObjectMapper()
    .registerModule(DefaultScalaModule)
  implicit lazy val timeout: Timeout = Timeout(5, TimeUnit.SECONDS)
  implicit val ec: ExecutionContextExecutorService = ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(10))

  val airlineRoutes: Route =
    pathPrefix(IntNumber) {
      brokerId => {
        pathPrefix(Segment) {
          lineId => {
            path("book") {
              post {
                entity(as[Ticket]) { ticket =>
                  airlineBrokers.get(brokerId)
                    .map(brokerRef => (brokerRef ? GetRequest(AirlineBrokerRequest(lineId, BookTicketCommand(ticket)))).mapTo[Try[BigDecimal]]
                       .flatMap(Future.fromTry))
                    .map (result =>  onComplete(result) {
                      case Success(value) => complete(objectMapper.writeValueAsString(value))
                      case Failure(exception) => complete(HttpResponse(StatusCodes.ImATeapot, entity = exception.getMessage))
                    }) match {
                    case Some(a) => a
                    case None => complete(HttpResponse(StatusCodes.ImATeapot,entity = s"Cannot find airline broker with id: $brokerId"))
                  }
                }
              }

            } ~
            path("status") {
              get {
                airlineBrokers.get(brokerId)
                  .map(brokerRef => (brokerRef ? GetRequest(AirlineBrokerRequest(lineId, GetFlightsCommand()))).mapTo[List[Flight]])
                  .map (result =>  onComplete(result) {
                    case Success(value) => complete(objectMapper.writeValueAsString(value))
                    case Failure(exception) => complete(HttpResponse(StatusCodes.ImATeapot, entity = exception.getMessage))
                  }) match {
                  case Some(a) => a
                  case None => complete(HttpResponse(StatusCodes.ImATeapot,entity = s"Cannot find airline broker with id: $brokerId"))
                }
              }
            }
          }
        }
      }
    }
}