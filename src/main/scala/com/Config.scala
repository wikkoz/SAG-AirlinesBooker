package com

case class Config (
  brokers :Int =  20,
  airlines: Int = 3,
  brokersThreads: Int = 20,
  routersThreads: Int = 10,
)
