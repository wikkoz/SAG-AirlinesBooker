package gatling

import gatling.scenario._
import io.gatling.app.Gatling
import io.gatling.core.config.GatlingPropertiesBuilder

object GatlingRunner {

  def main(args: Array[String]) {


    // This sets the class for the simulation we want to run.
    val simClass = classOf[MultiBrokerBookingScenario].getName

    val props = new GatlingPropertiesBuilder
    props.simulationClass(simClass)

    Gatling.fromMap(props.build)

  }
}
