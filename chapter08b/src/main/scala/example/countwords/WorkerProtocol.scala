package example.countwords

import akka.actor.typed.ActorRef
import akka.actor.typed.receptionist.ServiceKey

object WorkerProtocol {
  sealed trait Command
  final case class Process(text: String, replyTo: ActorRef[MasterProtocol.Event]) extends Command with CborSerializable

  val RegistrationKey = ServiceKey[WorkerProtocol.Command]("Worker")

}
