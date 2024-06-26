package routers

import akka.actor.typed.receptionist.ServiceKey
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors

object PhotoProcessor {

    val Key = ServiceKey[String]("photo-processor-key")

    def apply() : Behavior[String] = Behaviors.ignore
}