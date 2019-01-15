package com.airline.api

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import com.airline.domain.Ticket
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol{

  implicit val ticketJsonFormat: RootJsonFormat[Ticket] = jsonFormat2(Ticket)
}