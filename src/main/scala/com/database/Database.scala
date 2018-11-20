package com.database

import com.shop.domain.{Order, Product, Stock}

trait WithId {
	def id: Long
}

final case class StockEntity(id: Long, products: Seq[StockProductEntity]) extends WithId

final case class OrderEntity(id: Long, timestamp: Long, products: Seq[OrderProductEntity]) extends WithId

final case class ProductEntity(id: Long, barCode: Int, name: String) extends WithId

final case class OrderProductEntity(id: Long, product: ProductEntity, amount: Int) extends WithId

final case class StockProductEntity(id: Long, product: ProductEntity, amount: Int) extends WithId

class Database(
	              private var productsEntities: List[ProductEntity] = List(ProductEntity(1, 100, "coś"), ProductEntity(2, 200, "inne")),
	              private var stockEntity: StockEntity = StockEntity(1, List(StockProductEntity(1, ProductEntity(1, 100, "coś"), 20), StockProductEntity(2, ProductEntity(2, 200, "inne"), 30))),
	              private var ordersEntities: List[OrderEntity] = List[OrderEntity]()) extends DatabaseEntitiesMapper {

	def findStockProductByBarCode(barCode: Int): Option[Product] = stockEntity.products.find(entity => entity.product.barCode == barCode)
		.map(stockProductEntityToProduct)

	def findOrder(id: Long): Option[Order] = ordersEntities.find(_.timestamp == id).map(orderEntityToOrder)

	def findStock(): Stock = stockEntityToStock(stockEntity)

	def saveOrder(order: Order): Order = {
		val id = findNextDatabaseId(ordersEntities)
		val orderProducts = for {
			product <- order.products
			productEntity <- productsEntities.find(_.barCode == product.barCode)
		} yield OrderProductEntity(productEntity.id, productEntity, product.amount)
		val orderEntity = OrderEntity(id, order.id.get, orderProducts)
		ordersEntities = orderEntity :: ordersEntities
		orderEntityToOrder(orderEntity)
	}

	def updateStock(stock: Stock): Stock = {
		val updatedStock = for {
			stockProduct <- stockEntity.products
			product <- stock.products.find(_.barCode == stockProduct.product.barCode)
		} yield StockProductEntity(stockProduct.id, stockProduct.product, product.amount)

		stockEntity = StockEntity(stockEntity.id, updatedStock)
		stockEntityToStock(stockEntity)
	}

	private def findNextDatabaseId(list: List[WithId]) =
		if (list.isEmpty) 0L else list.maxBy(_.id).id + 1
}
