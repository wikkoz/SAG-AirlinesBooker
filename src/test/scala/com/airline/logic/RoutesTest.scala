package com.airline.logic

import akka.actor.ActorRef
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.testkit.TestProbe
import com.airline.api.AirlineRouter
import com.airline.domain.{AirlineBrokerRequest, BookTicketCommand, Ticket}
import org.scalatest._
import org.scalatest.concurrent.ScalaFutures

import scala.language.postfixOps


class RoutesTest extends WordSpec with BeforeAndAfterEach with ScalaFutures
  with AirlineRouter with Matchers with ScalatestRouteTest {

  lazy val routes: Route = airlineRoutes

  var airlineActors: Map[String, ActorRef] = _

  override def airlineBrokers: Map[Int, ActorRef] = Map.apply(0 -> airlineBroker.ref)

  var airlineActor: TestProbe = _
  var airlineBroker: TestProbe = _

  override def beforeEach() {
    airlineActor = TestProbe()
    airlineActors = Map("id" -> airlineActor.ref)
    airlineBroker = TestProbe()
  }

  "An Airlines route" when {
    "called booking tickets POST" should {
      val ticket: Ticket = Ticket(List(1, 2, 3), 123)
      val ticketJsonString = "{\n\"seats\": [1,2,3],\n\"flightId\": 123\n}"
      val ticketHttpEntity = HttpEntity(ContentTypes.`application/json`, ticketJsonString)

      "successfully book seats in specified plane" in {
        val request = HttpRequest(
          method = HttpMethods.POST,
          uri = "/0/id/book",
          entity = ticketHttpEntity
        )
        val result = request ~> routes ~> runRoute

        check {
          status should ===(StatusCodes.Created)
        }(result)

        val expectedAirlineBrokerMsg = AirlineBrokerRequest("id", BookTicketCommand(ticket))
        airlineBroker.expectMsg(expectedAirlineBrokerMsg)
      }

      "called with bad broker id" in {
        val request = HttpRequest(
          method = HttpMethods.POST,
          uri = "/100/id/book",
          entity = ticketHttpEntity
        )
        val result = request ~> routes ~> runRoute

        check {
          status should ===(StatusCodes.NotFound)
        }(result)
      }
    }
  }
}
