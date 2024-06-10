package example.countwords

import akka.actor.typed.ActorRef

object MasterProtocol {
  sealed trait Event
  final case object Tick extends Event
  final case class CountedWords(aggregation: Map[String, Int]) extends Event with CborSerializable
  final case class FailedJob(text: String) extends Event

}
