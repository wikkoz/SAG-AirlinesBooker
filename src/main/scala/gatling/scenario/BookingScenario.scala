package gatling.scenario

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.http.request.builder.HttpRequestBuilder

class BookingScenario extends Simulation {
  val httpProtocol = http
    .baseUrl("http://localhost:8080") // Here is the root for all relative URLs
    .acceptHeader("text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8") // Here are the common headers
    .doNotTrackHeader("1")
    .acceptLanguageHeader("en-US,en;q=0.5")
    .acceptEncodingHeader("gzip, deflate")
    .userAgentHeader("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.8; rv:16.0) Gecko/20100101 Firefox/16.0")

  val headers_10 = Map("Content-Type" -> "application/x-www-form-urlencoded") // Note the headers specific to a given request

  object BookSeats {
    val bookSeats = exec(http("Book seats")
      .put("/api/1/1/book")
      .headers(Map(
        "Accept" -> "application/json, text/javascript, */*; q=0.01",
        "Content-Type" -> "application/json"))
      .body(StringBody("""{"seats":[6,7],"flightId":1}""")).asJson)

    val bookSeatsInvalid = exec(http("Book seats")
      .put("/api/1/1/book")
      .headers(Map(
        "Accept" -> "application/json, text/javascript, */*; q=0.01",
        "Content-Type" -> "application/json"))
      .body(StringBody("""{"seats":[6,7],"flightId":1}""")).asJson
      .check(status.is(418)))

  }

  val scn = scenario("Multibroker scenario") // A scenario is a chain of requests and pauses
    .exec(BookSeats.bookSeats)
    .pause(5)
    .exec(BookSeats.bookSeatsInvalid)

  setUp(scn.inject(atOnceUsers(1)).protocols(httpProtocol))
}