package com.airline.api

import akka.actor.ActorRef
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{ExceptionHandler, Route}
import com.airline.logic.exception.ValidationException

import scala.concurrent.Future

trait AirlineRouter extends JsonSupport {
	def airlineBrokers: Map[Int, ActorRef]

	val buyExceptionHandler = ExceptionHandler {
		case e: ValidationException =>
			extractUri { _ =>
				complete(HttpResponse(422, entity = e.getMessage))
			}
	}

	val shopRoutes: Route =
		pathPrefix("shop") {
			handleExceptions(buyExceptionHandler) {
				pathPrefix("order" / Segment) {
					orderId => {
						get {
							complete(shopService.getOrder(orderId.toLong))
						}
					}
				}
			} ~
			pathEnd {
				get {
					val stock: Future[Stock] = shopService.getAllProducts
					complete(stock)
				}
			} ~
			handleExceptions(buyExceptionHandler) {
				path("buy") {
					post {
						entity(as[Order]) {
							order => complete(shopService.buyProducts(order))
						}
					}
				}
			}
		}
}
