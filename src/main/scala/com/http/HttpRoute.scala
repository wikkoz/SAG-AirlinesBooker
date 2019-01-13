package com.http

import akka.actor.ActorSystem
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.airline.api.AirlineRouter

trait HttpRoute extends AirlineRouter {

	def system: ActorSystem

	val route: Route =
		pathPrefix("api") {
			airlineRoutes
		}
}
