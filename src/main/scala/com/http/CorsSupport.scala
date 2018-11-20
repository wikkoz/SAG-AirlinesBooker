package com.http

import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model.headers._
import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{Directive0, Route}

trait CorsSupport {
	private val corsResponseHeaders = List(
		`Access-Control-Allow-Origin`.*,
		`Access-Control-Allow-Credentials`(true),
		`Access-Control-Allow-Headers`("Authorization",
			"Content-Type", "X-Requested-With")
	)
	private val addAccessControlHeaders: Directive0 = {
		respondWithHeaders(corsResponseHeaders)
	}
	//this handles preflight OPTIONS requests.
	private def preflightRequestHandler: Route = options {
		complete(HttpResponse(StatusCodes.OK).
			withHeaders(`Access-Control-Allow-Methods`(OPTIONS, POST, PUT, GET, DELETE)))
	}
	// Wrap the Route with this method to enable adding of CORS headers
	def corsHandler(r: Route): Route = addAccessControlHeaders {
		preflightRequestHandler ~ r
	}
	// Helper method to add CORS headers to HttpResponse
	// preventing duplication of CORS headers across code
	def addCORSHeaders(response: HttpResponse):HttpResponse =
		response.withHeaders(corsResponseHeaders)

}