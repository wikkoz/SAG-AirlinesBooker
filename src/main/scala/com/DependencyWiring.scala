package com

import akka.actor.{ActorRef, ActorSystem, Props}
import com.airline.logic.AirlineActor

import scala.concurrent.ExecutionContext

trait DependencyWiring {
	def system: ActorSystem

	def executionContext: ExecutionContext
}
