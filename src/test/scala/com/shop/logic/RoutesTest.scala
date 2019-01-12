package com.shop.logic

import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.testkit.TestProbe
import com.shop.api.ShopRoutes
import com.shop.domain.{Order, Product, Stock}
import com.shop.logic.AirlineActor.{Buy, GetOrder, GetStock}
import com.shop.logic.exception.ValidationException
import org.scalatest._
import org.scalatest.concurrent.ScalaFutures

import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.{Failure, Success}


class RoutesTest extends WordSpec with BeforeAndAfterEach with ScalaFutures
	with ShopRoutes with Matchers with ScalatestRouteTest {

	lazy val routes: Route = shopRoutes

	override def shopService: ShopService = new ShopService(shopActor.ref)

	var shopActor: TestProbe = _

	override def beforeEach() {
		shopActor = TestProbe()
	}

	"A ShopRoutes" when {
		"called GET (/shop) " should {
			"return stock from ShopActor" in {
				val request = HttpRequest(uri = "/shop")
				val stock = Stock(List(Product("meat", 100, 20)))
				val result = request ~> routes ~> runRoute
				shopActor.expectMsg(0 second, GetStock())
				shopActor.reply(stock)

				check {
					status should ===(StatusCodes.OK)
					contentType should ===(ContentTypes.`application/json`)
					entityAs[String] shouldEqual """{"products":[{"name":"meat","barCode":100,"amount":20}]}"""
				}(result)

			}
		}
		"called GET (/shop/order) " should {
			"return order when there is an order with id" in {
				val request = HttpRequest(uri = "/shop/order/1")
				val order = Success(Order(List(Product("meat", 100, 20)), Some(1L)))
				val result = request ~> routes ~> runRoute
				shopActor.expectMsg(0 second, GetOrder(1))
				shopActor.reply(order)

				check {
					status should ===(StatusCodes.OK)
					contentType should ===(ContentTypes.`application/json`)
					entityAs[String] shouldEqual """{"products":[{"name":"meat","barCode":100,"amount":20}],"id":1}"""
				}(result)

			}
		}
		"called GET (/shop/order) " should {
			"return exception when there is no order with id" in {
				val request = HttpRequest(uri = "/shop/order/1")
				val order = Failure(new ValidationException("There is no order with id 1"))
				val result = request ~> routes ~> runRoute
				shopActor.expectMsg(0 second, GetOrder(1))
				shopActor.reply(order)

				check {
					status should ===(StatusCodes.UnprocessableEntity)
				}(result)
			}
		}
		"called POST (/shop/buy) " should {
			"return the order when there is no validation errors" in {
				val order = Order(List(Product("meat", 100, 20)), Some(1L))
				val orderEntity = Marshal(order).to[MessageEntity].futureValue
				val request = HttpRequest(uri = "/shop/buy", method = HttpMethods.POST).withEntity(orderEntity)
				val result = request ~> routes ~> runRoute
				shopActor.expectMsg(0 second, Buy(order))
				shopActor.reply(Success(order))

				check {
					status should ===(StatusCodes.OK)
					contentType should ===(ContentTypes.`application/json`)
					entityAs[String] shouldEqual """{"products":[{"name":"meat","barCode":100,"amount":20}],"id":1}"""
				}(result)
			}
		}

		"called POST (/shop/buy) " should {
			"return exception when there is validation error" in {
				val order = Order(List(Product("meat", 100, 20)), Some(1L))
				val orderEntity = Marshal(order).to[MessageEntity].futureValue
				val request = HttpRequest(uri = "/shop/buy", method = HttpMethods.POST).withEntity(orderEntity)
				val result = request ~> routes ~> runRoute
				shopActor.expectMsg(0 second, Buy(order))
				shopActor.reply(Failure(new ValidationException("There is no meat on stock")))

				check {
					status should ===(StatusCodes.UnprocessableEntity)
				}(result)
			}
		}
	}


}
