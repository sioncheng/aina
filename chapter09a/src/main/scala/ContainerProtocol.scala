package example.sharding

import akka.actor.typed.{ActorRef, Behavior}
import akka.cluster.sharding.typed.scaladsl.EntityTypeKey

object ContainerProtocol {
  final case class Cargo(id: String, kind: String, size: Int)

  sealed trait Command
  final case class AddCargo(cargo: Cargo)
    extends Command
    with CborSerializable
  final case class GetCargos(replyTo: ActorRef[List[Cargo]])
    extends Command
    with CborSerializable

  val typeKey = EntityTypeKey[ContainerProtocol.Command]("container-type-key")

}
