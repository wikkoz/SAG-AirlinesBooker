package gatling.scenario

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.http.request.builder.HttpRequestBuilder

class BasicScenario extends Simulation {
  val httpProtocol = http
    .baseUrl("http://localhost:8080") // Here is the root for all relative URLs
    .acceptHeader("text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8") // Here are the common headers
    .doNotTrackHeader("1")
    .acceptLanguageHeader("en-US,en;q=0.5")
    .acceptEncodingHeader("gzip, deflate")
    .userAgentHeader("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.8; rv:16.0) Gecko/20100101 Firefox/16.0")

  val headers_10 = Map("Content-Type" -> "application/x-www-form-urlencoded") // Note the headers specific to a given request


  object CheckStatus {
    val checkStatus =     exec(http("Checking status")
      .get("/api/1/1/status"))
  }

  object BookSeats {
    val bookSeats = exec(http("Book seats")
      .put("/api/1/1/book")
      .headers(Map(
        "Accept" -> "application/json, text/javascript, */*; q=0.01",
        "Content-Type" -> "application/json"))
      .body(StringBody("""{"seats":[4,5],"flightId":1}""")).asJson)    //   .formParam("seats", "[1,2,3]")  .formParam("flightId", 1)) // add ticket body

  }

  val scn = scenario("Basic scenario") // A scenario is a chain of requests and pauses
    .exec(CheckStatus.checkStatus, BookSeats.bookSeats)


  setUp(scn.inject(atOnceUsers(1)).protocols(httpProtocol))
}
