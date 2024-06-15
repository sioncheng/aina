package example.sharding

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import org.slf4j.LoggerFactory

object Container {
  private val logger = LoggerFactory.getLogger("container")

  def apply(id: String) : Behavior[ContainerProtocol.Command] = {
    logger.info(s"container apply $id")
    ready(List())
  }

  private def ready(cargos: List[ContainerProtocol.Cargo]) : Behavior[ContainerProtocol.Command] =
    Behaviors.receiveMessage[ContainerProtocol.Command] {
      case ContainerProtocol.AddCargo(cargo) =>
        ready(cargo +: cargos)
      case ContainerProtocol.GetCargos(replyTo) =>
        replyTo ! cargos
        Behaviors.same
    }
}