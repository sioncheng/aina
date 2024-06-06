import akka.actor.typed.ActorRef
import akka.actor.typed.scaladsl.Behaviors
import scala.concurrent.duration._

object SimplifiedManager {

  sealed trait Command
  final case class CreateChild(name: String) extends Command
  final case class Forward(message: String, sendTo: ActorRef[String])
      extends Command

  def apply(): Behaviors.Receive[Command] =
    Behaviors.receive { (context, message) =>
      message match {
        case CreateChild(name) =>
          context.spawn(SimplifiedWorker(), name)
          Behaviors.same
        case Forward(message, sendTo) =>
          sendTo ! message
          Behaviors.same
      }
    }
}
