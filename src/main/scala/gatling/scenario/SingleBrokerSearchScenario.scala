package gatling.scenario

import io.gatling.core.Predef.{Simulation, atOnceUsers, exec, scenario}
import io.gatling.http.Predef.http

import scala.util.Random

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.http.request.builder.HttpRequestBuilder

class SingleBrokerSearchScenario extends Simulation {
  val httpProtocol = http
    .baseUrl("http://localhost:8080") // Here is the root for all relative URLs
    .acceptHeader("text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8") // Here are the common headers
    .doNotTrackHeader("1")
    .acceptLanguageHeader("en-US,en;q=0.5")
    .acceptEncodingHeader("gzip, deflate")
    .userAgentHeader("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.8; rv:16.0) Gecko/20100101 Firefox/16.0")

  val headers_10 = Map("Content-Type" -> "application/x-www-form-urlencoded") // Note the headers specific to a given request


  object CheckStatus {

    val checkStatus = exec(http("Checking status")
      .get("/api/1/1/status"))
  }


  val scn = scenario("Single broker search scenario")
    .exec(CheckStatus.checkStatus)


  setUp(scn.inject(atOnceUsers(200)).protocols(httpProtocol))
}
