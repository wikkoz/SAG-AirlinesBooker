package com.shop.logic

import java.util.concurrent.TimeUnit

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.pattern.ask
import akka.testkit.TestKit
import akka.util.Timeout
import com.database.{Database, ProductEntity, StockEntity, StockProductEntity}
import com.shop.domain.{Order, Product, Stock}
import com.shop.logic.ShopActor.{Buy, GetOrder, GetStock}
import org.scalatest.{BeforeAndAfterEach, WordSpecLike}

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.Try

class ShopActorTest(_system: ActorSystem) extends TestKit(_system) with WordSpecLike with BeforeAndAfterEach {

	def this() = this(ActorSystem("ShopActorTestSpec"))

	implicit lazy val timeout: Timeout = Timeout(5, TimeUnit.SECONDS)

	var shopActor: ActorRef = _

	override def beforeEach() {
		val database = new Database(List(ProductEntity(1, 100, "meat")),
			StockEntity(1, List(StockProductEntity(1, ProductEntity(1, 100, "meat"), 30))))
		shopActor = system.actorOf(Props(new ShopActor(database)))
	}

	"A shop" when {
		"called get stock " should {
			"have return correct values in shop" in {
				val stock = Await.result((shopActor ? GetStock()).mapTo[Stock], 5 second)

				assert(stock.products.size == 1)
				assert(stock.products.map(p => p.name).contains("meat"))
				assert(stock.products.map(p => p.amount).contains(30))
			}
		}
		"there is an order " should {
			"save this order" in {
				val order = Order(List(Product("meat", 100, 15)), None)
				val orderId = Await.result((shopActor ? Buy(order)).mapTo[Try[Order]], 5 second).map(_.id.get).get
				val savedOrdered =  Await.result((shopActor ? GetOrder(orderId)).mapTo[Try[Order]], 5 second).get

				assert(order.products == savedOrdered.products)

			}
			"lower size of product in stock" in {
				val order = Order(List(Product("meat", 100, 10)), None)
				shopActor ? Buy(order)
				val stock = Await.result((shopActor ? GetStock()).mapTo[Stock], 5 second)
				assert(stock.products.find(_.barCode == 100).get.amount == 20)
			}

			"throw exception when there is less product in stock" in {
				val order = Order(List(Product("meat", 100, 40)), None)
				assert(Await.result((shopActor ? Buy(order)).mapTo[Try[Order]], 5 second).isFailure)
			}

			"throw exception when there is negative amount of ordered products" in {
				val order = Order(List(Product("meat", 100, -40)), None)
				assert(Await.result((shopActor ? Buy(order)).mapTo[Try[Order]], 5 second).isFailure)
			}

			"throw exception when there is no such product in stock" in {
				val order = Order(List(Product("socks", 200, 10)), None)
				assert(Await.result((shopActor ? Buy(order)).mapTo[Try[Order]], 5 second).isFailure)
			}

			"should correctly check condtions when there is multiple orders" in {
				val order = Order(List(Product("meat", 100, 5)), None)
				shopActor ? Buy(order)
				shopActor ? Buy(order)
				shopActor ? Buy(order)
				val stock = Await.result((shopActor ? GetStock()).mapTo[Stock], 5 second)

				assert(stock.products.find(_.barCode == 100).get.amount == 15)
			}
		}
	}


}
