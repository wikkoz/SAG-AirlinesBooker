package com.shop.logic

import java.util.concurrent.{Executors, TimeUnit}

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import com.shop.domain.{Order, Stock}
import com.shop.logic.ShopActor.{Buy, GetOrder, GetStock}

import scala.concurrent.{ExecutionContext, ExecutionContextExecutorService, Future}
import scala.util.Try

class ShopService(val shopActor: ActorRef) {

	implicit lazy val timeout: Timeout = Timeout(5, TimeUnit.SECONDS)
	implicit val ec: ExecutionContextExecutorService = ExecutionContext.fromExecutorService(Executors.newSingleThreadExecutor)

	def getAllProducts: Future[Stock] = {
		(shopActor ? GetStock()).mapTo[Stock]
	}

	def buyProducts(order: Order): Future[Order] = {
		(shopActor ? Buy(order)).mapTo[Try[Order]]
			.flatMap(Future.fromTry)
	}

	def getOrder(orderId: Long): Future[Order] = {
		(shopActor ? GetOrder(orderId)).mapTo[Try[Order]]
			.flatMap(Future.fromTry)
	}
}
