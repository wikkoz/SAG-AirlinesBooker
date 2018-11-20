package com

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.http.{CorsSupport, HttpRoute}

import scala.concurrent.ExecutionContext

object Application extends App with CorsSupport {

	def startApplication() = {
		implicit val actorSystem: ActorSystem = ActorSystem()
		implicit val executor: ExecutionContext = actorSystem.dispatcher
		implicit val materializer: ActorMaterializer = ActorMaterializer()

		val modules: DependencyWiring with HttpRoute = new DependencyWiring with HttpRoute {
			lazy val system: ActorSystem = actorSystem
			lazy val executionContext: ExecutionContext = executor
		}
		actorSystem.log.info("Starting server on localhost:8080")
		Http().bindAndHandle(corsHandler(modules.route), "localhost", 8080)
	}

	startApplication()
}
