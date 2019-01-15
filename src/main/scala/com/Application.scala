package com

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.http.{CorsSupport, HttpRoute}
import scopt.OptionParser

import scala.concurrent.ExecutionContext

object Application extends App with CorsSupport {

  def startApplication() = {
    implicit val actorSystem: ActorSystem = ActorSystem()
    implicit val executor: ExecutionContext = actorSystem.dispatcher
    implicit val materializer: ActorMaterializer = ActorMaterializer()

    val parser = new scopt.OptionParser[Config]("airlines") {
      opt[Int]('b', "brokers").action( (x, c) =>
        c.copy(brokers = x) ).text("Number of brokers")
      opt[Int]('a', "airlines").action( (x, c) =>
        c.copy(airlines = x) ).text("Number of airlines")
      opt[Int]('t', "broker_threads").action( (x, c) =>
        c.copy(brokersThreads = x) ).text("Number of threads per broker")
      opt[Int]('r', "router_threads").action( (x, c) =>
        c.copy(routersThreads = x) ).text("Number of threads in router")

    }

    parser.parse(args, Config()) match {
      case Some(c) =>
        val modules: DependencyWiring with HttpRoute = new DependencyWiring with HttpRoute {
          lazy val system: ActorSystem = actorSystem
          lazy val config: Config = c
          lazy val executionContext: ExecutionContext = executor
        }
        actorSystem.log.info("Starting server on localhost:8080")
        Http().bindAndHandle(corsHandler(modules.route), "localhost", 8080)
      case None =>
    }
  }
  startApplication()
}
