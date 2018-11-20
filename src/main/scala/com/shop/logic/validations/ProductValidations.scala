package com.shop.logic.validations

import com.shop.domain.Product
import com.shop.logic.exception.ValidationException

import scala.util.{Failure, Success, Try}

trait ProductValidations {
	def validateProducts(products: Try[(Product, Product)]): Try[(Product, Product)] = {
		products
			.flatMap(checkAvailability)
			.flatMap(checkProductAmount)
	}

	private def checkProductAmount(products: (Product, Product)): Try[(Product, Product)] = {
		products match {
			case (op, _) if op.amount < 0 => Failure(new ValidationException(s"Cannot buy less than 0 products of ${op.name}."))
			case (op, ep) => Success(op, ep)
		}
	}

	private def checkAvailability(products: (Product, Product)): Try[(Product, Product)] = {
		products match {
			case (op, ep) if ep.amount < op.amount => Failure(new ValidationException(s"Cannot buy ${op.amount} products of ${op.name}:" +
				s". There is only ${ep.amount} in stock."))
			case (op, ep) => Success(op, ep)
		}
	}
}
