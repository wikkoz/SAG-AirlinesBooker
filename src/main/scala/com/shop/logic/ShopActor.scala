package com.shop.logic

import akka.actor.{Actor, ActorLogging}
import com.database.Database
import com.shop.domain.{Order, Product, Stock}
import com.shop.logic.ShopActor.{Buy, GetOrder, GetStock}
import com.shop.logic.exception.ValidationException
import com.shop.logic.validations.{OrderValidations, ProductValidations}

import scala.util.{Failure, Success, Try}

object ShopActor {

	final case class Buy(order: Order)

	final case class GetStock()

	final case class GetOrder(orderId: Long)

}

class ShopActor(private val database: Database) extends Actor with ActorLogging with OrderValidations
	with ProductValidations with BarCodeGenerator {

	override def receive: Receive = {
		case GetStock() =>
			sender() ! database.findStock()
		case Buy(order: Order) =>
			sender() ! buy(order)
		case GetOrder(orderId: Long) =>
			sender() ! getOrder(orderId)
	}

	private def buy(order: Order): Try[Order] = {
		validate(order)
			.map(commit)
	}

	private def validate(order: Order): Try[Order] = {
		val orderValidation = validateOrder(order)
		if (orderValidation.isFailure) {
			return orderValidation
		}

		val connectedProducts = order.products
			.map(product => (product, getProduct(product.barCode)))
			.map({
				case (op, Success(ep)) => Success(op, ep)
				case (_, Failure(ep)) => Failure(ep)
			})

		val productsValidation = connectedProducts.map(validateProducts)

		val (_, failures) = productsValidation.partition(_.isSuccess)

		if (failures.isEmpty) {
			Success(order)
		} else {
			Failure(new ValidationException(failures.map({
				case Failure(exception) => exception.getMessage
				case _ => ""
			}).mkString("\n")))
		}
	}


	private def updateStock(order: Order, stock: Stock): Stock = {
		val updatedProducts = for {
			stockProduct <- stock.products
			product <- order.products.find(_.barCode == stockProduct.barCode)
		} yield Product(product.name, product.barCode, stockProduct.amount - product.amount)

		Stock(updatedProducts)
	}

	private def commit(order: Order): Order = {
		val stock = database.findStock()
		val updatedStock = updateStock(order, stock)
		database.updateStock(updatedStock)
		val orderToSave = Order(order.products, Some(generateBarCode()))
		val savedOrder = database.saveOrder(orderToSave)
		savedOrder
	}

	private def getOrder(orderId: Long): Try[Order] = {
		database.findOrder(orderId) match {
			case None => Failure(new ValidationException(s"Cannot find order: $orderId"))
			case Some(o) => Success(o)
		}
	}

	private def getProduct(barCode: Int): Try[Product] = {
		database.findStockProductByBarCode(barCode) match {
			case None => Failure(new ValidationException(s"Cannot find product with bar code: $barCode"))
			case Some(p) => Success(p)
		}
	}
}