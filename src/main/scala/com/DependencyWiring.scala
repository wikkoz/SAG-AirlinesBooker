package com

import akka.actor.{ActorRef, ActorSystem}
import com.airline.logic.{AirlineActor, AirlineBrokerActor}

import scala.concurrent.ExecutionContext

trait DependencyWiring {
  final val airlines: List[String] = List("first", "second", "third")

  private def initAirlineBrokersActors(n: Int): Map[Int, ActorRef] = {
    Array.range(0, n)
      .map(i => Tuple2(i, getAirlineBroker(i, "AirlineBrokerActor" + i)))
      .toMap
  }

  private def getAirlineBroker(i: Int, name: String): ActorRef = {
    system.actorOf(AirlineBrokerActor.props(getAirlinesActors(i)), name)
  }

  private def getAirlinesActors(i: Int): Map[String, ActorRef] = {
    airlines
      .map(airline => Tuple2(airline, system.actorOf(AirlineActor.props("AirlineActor_" + airline + i))))
      .toMap
  }

  def system: ActorSystem

  def executionContext: ExecutionContext

  val airlineBrokers: Map[Int, ActorRef] = initAirlineBrokersActors(4)
}
