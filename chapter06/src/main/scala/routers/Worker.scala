package routers

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.Behavior
import akka.actor.typed.ActorRef

object Worker {
  def apply(monitor: ActorRef[String]): Behavior[String] = 
    Behaviors.receiveMessage[String] {
        case message =>
            monitor ! message
            Behaviors.same
    }
}
