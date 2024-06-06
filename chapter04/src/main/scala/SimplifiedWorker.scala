import akka.actor.typed.javadsl.Behaviors

object SimplifiedWorker {
  def apply() = Behaviors.ignore[String]
}
