package simplequestion

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors

import scala.util.Random

object Worker {

    sealed trait Command
    final case class Parse(replyTo: ActorRef[Worker.Response]) extends Command

    sealed trait Response
    final case object Done extends Response
}
