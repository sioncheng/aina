package example.http

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import akka.cluster.sharding.typed.scaladsl.EntityTypeKey

object Container {

  final case class Cargo(kind: String, size: Int)
  final case class Cargos(cargos: List[Cargo])

  sealed trait Command
  final case class AddCargo(cargo: Cargo) extends Command
  final case class GetCargos(replyTo: ActorRef[Cargos]) extends Command

  val TypeKey = EntityTypeKey[Command]("container")

  def apply(entityId: String,
            cargos: List[Cargo] = Nil) : Behavior[Command] = {
    Behaviors.receive { (ctx, msg) =>
      msg match {
        case AddCargo(cargo) =>
          println(s"adding cargo $cargo")
          apply(entityId, cargo +: cargos)
        case GetCargos(replyTo) =>
          replyTo ! Cargos(cargos)
          Behaviors.same
      }
    }
  }
}
