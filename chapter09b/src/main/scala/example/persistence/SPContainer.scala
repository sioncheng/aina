package example.persistence

import akka.actor.typed.{ActorRef, Behavior, SupervisorStrategy}
import akka.cluster.sharding.typed.scaladsl.EntityTypeKey
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.RetentionCriteria
import akka.persistence.typed.scaladsl.EventSourcedBehavior
import akka.persistence.typed.scaladsl.Effect

import scala.concurrent.duration.DurationInt

object SPContainer {

  final case class Cargo(id: String, kind: String, size: Int)

  sealed trait Command
  final case class AddCargo(cargo: Cargo) extends Command with CborSerializable
  final case class GetCargos(replyTo: ActorRef[List[Cargo]]) extends Command with CborSerializable

  sealed trait Event
  final case class CargoAdded(containerId: String, cargo: Cargo) extends Event with CborSerializable

  final case class State(cargos: List[Cargo] = Nil)

  val typeKey = EntityTypeKey[SPContainer.Command]("spcontainer-type-key")

  def apply(containerId: String): Behavior[Command] =
    EventSourcedBehavior[Command, Event, State] (
      PersistenceId(typeKey.name, containerId),
      State(),
      (state, command) => commandHandler(containerId, state, command),
      eventHandler
    ).withTagger {
      case _ => Set("container-tag-" + containerId.toInt % 3)
    }
      .withRetention(RetentionCriteria.snapshotEvery(numberOfEvents = 100, keepNSnapshots = 2))
      .onPersistFailure(SupervisorStrategy.restartWithBackoff(
        minBackoff = 10.seconds,
        maxBackoff = 60.seconds,
        randomFactor = 0.1
      ))

  private def commandHandler(containerId: String, state: State, command: Command): Effect[Event, State] =
    command match {
      case AddCargo(cargo) =>
        Effect.persist(CargoAdded(containerId, cargo))
      case GetCargos(replyTo) =>
        Effect.none.thenRun( state => replyTo ! state.cargos)
    }

  private def eventHandler(state: State, event: Event): State =
    event match {
      case CargoAdded(containerId, cargo) =>
        state.copy(cargos = cargo +: state.cargos)
    }
}
