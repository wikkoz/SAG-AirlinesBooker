package com.shop.logic

trait BarCodeGenerator {
	def generateBarCode(): Long = System.nanoTime()
}
