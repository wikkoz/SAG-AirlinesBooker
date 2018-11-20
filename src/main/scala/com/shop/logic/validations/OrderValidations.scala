package com.shop.logic.validations

import com.shop.domain.Order
import com.shop.logic.exception.ValidationException

import scala.util.{Failure, Success, Try}

trait OrderValidations extends ProductValidations {
	def validateOrder(order: Order): Try[Order] = {
		if (order.products.forall(product => product.amount <= 0)) {
			Failure(new ValidationException("Cannot make an order when all products have amount 0."))
		} else {
			Success(order)
		}
	}
}
