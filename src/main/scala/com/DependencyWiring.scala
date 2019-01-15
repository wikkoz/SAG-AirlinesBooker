package com

import akka.actor.{ActorRef, ActorSystem}
import com.airline.logic.{AirlineActor, AirlineBrokerActor}

import scala.concurrent.ExecutionContext

trait DependencyWiring {

  private def initAirlineBrokersActors(n: Int): Map[Int, ActorRef] = {
    Array.range(0, config.brokers)
      .map(i => Tuple2(i, getAirlineBroker(i)))
      .toMap
  }

  private def getAirlineBroker(i: Int): ActorRef = {
    system.actorOf(AirlineBrokerActor.props(getAirlinesActors(i), config.brokersThreads))
  }

  private def getAirlinesActors(i: Int): Map[Int, ActorRef] = {
    Array.range(0, config.airlines)
      .map(airline => Tuple2(airline, system.actorOf(AirlineActor.props(airline))))
      .toMap
  }

  def system: ActorSystem
  def config: Config
  def executionContext: ExecutionContext

  final val airlineBrokers: Map[Int, ActorRef] = initAirlineBrokersActors(4)
}
