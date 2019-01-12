package com

import akka.actor.{ActorRef, ActorSystem, Props}
import com.shop.logic.{AirlineActor, ShopService}

import scala.concurrent.ExecutionContext

trait DependencyWiring {
	def system: ActorSystem

	def executionContext: ExecutionContext

	val shopActor: ActorRef = system.actorOf(Props(new AirlineActor(database)), "shopActor")

	lazy val shopService: ShopService = new ShopService(shopActor)
}
