package com.shop.api

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import com.shop.domain.{Order, Product, Stock}
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {

	implicit val productJsonFormat: RootJsonFormat[Product] = jsonFormat3(Product)
	implicit val stockJsonFormat: RootJsonFormat[Stock] = jsonFormat1(Stock)
	implicit val orderJsonFormat: RootJsonFormat[Order] = jsonFormat2(Order)

}
