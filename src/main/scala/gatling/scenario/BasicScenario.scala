package gatling.scenario

import io.gatling.core.Predef._
import io.gatling.http.Predef._

class BasicScenario extends Simulation {
  val httpProtocol = http
    .baseUrl("http://localhost:8080") // Here is the root for all relative URLs
    .acceptHeader("text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8") // Here are the common headers
    .doNotTrackHeader("1")
    .acceptLanguageHeader("en-US,en;q=0.5")
    .acceptEncodingHeader("gzip, deflate")
    .userAgentHeader("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.8; rv:16.0) Gecko/20100101 Firefox/16.0")

  val headers_10 = Map("Content-Type" -> "application/x-www-form-urlencoded") // Note the headers specific to a given request

  val scn = scenario("Scenario Name") // A scenario is a chain of requests and pauses
    .exec(http("request_1")
    .get("/api/1/1/status"))
    .pause(7) // Note that Gatling has recorded real time pauses
    .exec(http("request_2")
    .put("/api/1/1/book")
    .headers(Map(
      "Accept" -> "application/json, text/javascript, */*; q=0.01",
      "Content-Type" -> "application/json"
    )
    )
    .body(StringBody("""{"seats":[1,2,3],"flightId":1}""")).asJson)    //   .formParam("seats", "[1,2,3]")  .formParam("flightId", 1)) // add ticket body


  //    .pause(2)
  //    .exec(http("request_3")
  //      .get("/computers/6"))
  //    .pause(3)
  //    .exec(http("request_4")
  //      .get("/"))
  //    .pause(2)
  //    .exec(http("request_5")
  //      .get("/computers?p=1"))
  //    .pause(670 milliseconds)
  //    .exec(http("request_6")
  //      .get("/computers?p=2"))
  //    .pause(629 milliseconds)
  //    .exec(http("request_7")
  //      .get("/computers?p=3"))
  //    .pause(734 milliseconds)
  //    .exec(http("request_8")
  //      .get("/computers?p=4"))
  //    .pause(5)
  //    .exec(http("request_9")
  //      .get("/computers/new"))
  //    .pause(1)
  //    .exec(http("request_10") // Here's an example of a POST request
  //      .post("/computers")
  //      .headers(headers_10)
  //      .formParam("name", "Beautiful Computer") // Note the triple double quotes: used in Scala for protecting a whole chain of characters (no need for backslash)
  //      .formParam("introduced", "2012-05-30")
  //      .formParam("discontinued", "")
  //      .formParam("company", "37"))

  setUp(scn.inject(atOnceUsers(2
  )).protocols(httpProtocol))
}
