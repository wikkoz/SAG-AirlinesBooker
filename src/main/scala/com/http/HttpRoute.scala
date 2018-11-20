package com.http

import akka.actor.ActorSystem
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.shop.api.ShopRoutes

trait HttpRoute extends ShopRoutes {

	def system: ActorSystem

	val route: Route =
		pathPrefix("api") {
			shopRoutes
		}
}
