package com.shop.domain

final case class Stock(products: Seq[Product])

final case class Order(products: Seq[Product], id: Option[Long])

final case class Product(name: String, barCode: Int, amount: Int)
