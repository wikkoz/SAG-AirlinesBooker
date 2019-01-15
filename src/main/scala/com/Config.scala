package com

case class Config (
  brokers :Int =  3,
  airlines: Int = 3,
  brokersThreads: Int = 3,
  routersThreads: Int = 10,
)
