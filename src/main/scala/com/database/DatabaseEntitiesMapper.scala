package com.database

import com.shop.domain.{Order, Product, Stock}

trait DatabaseEntitiesMapper {

	def stockEntityToStock(stockEntity: StockEntity): Stock = {
		Stock(stockEntity.products.map(stockProductEntityToProduct))
	}

	def stockProductEntityToProduct(stockProductEntity: StockProductEntity): Product = {
		Product(stockProductEntity.product.name, stockProductEntity.product.barCode, stockProductEntity.amount)
	}

	def orderProductEntityToProduct(orderProductEntity: OrderProductEntity): Product = {
		Product(orderProductEntity.product.name, orderProductEntity.product.barCode, orderProductEntity.amount)
	}

	def orderEntityToOrder(orderEntity: OrderEntity): Order = {
		Order(orderEntity.products.map(orderProductEntityToProduct), Some(orderEntity.timestamp))
	}
}
