package com

import akka.actor.{ActorRef, ActorSystem, Props}
import com.database.Database
import com.shop.logic.{ShopActor, ShopService}

import scala.concurrent.ExecutionContext

trait DependencyWiring {
	def system: ActorSystem

	def executionContext: ExecutionContext

	val database = new Database()
	val shopActor: ActorRef = system.actorOf(Props(new ShopActor(database)), "shopActor")

	lazy val shopService: ShopService = new ShopService(shopActor)
}
