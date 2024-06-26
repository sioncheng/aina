import akka.actor.typed.{Behavior, SupervisorStrategy}
import akka.cluster.sharding.typed.scaladsl.EntityTypeKey
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.{Effect, EventSourcedBehavior, ReplyEffect}
import protocol.MarketProtocol._

import java.time.{OffsetDateTime, ZoneId}
import scala.concurrent.duration.DurationInt

object Market {

  val TypeKey = EntityTypeKey[Command]("market")

  def apply(marketId: String): Behavior[Command] =
    EventSourcedBehavior[Command, Event, State] (
      PersistenceId(TypeKey.name, marketId),
      UninitializedState(Status.empty(marketId)),
      commandHandler,
      eventHandler
    ).onPersistFailure(
      SupervisorStrategy.restartWithBackoff(
        minBackoff = 10.seconds,
        maxBackoff = 60.seconds,
        randomFactor = 0.1
      )
    )

  private def commandHandler(state: State, command: Command) :
    ReplyEffect[Event, State] = {
    (state, command) match {
      case (state: UninitializedState, command: Open) =>
        open(state, command)
      case (state: OpenState, command: Update) =>
        update(state, command)
      case (state: State, command: Close) =>
        close(state, command)
      case (state: State, command: Cancel) =>
        cancel(state, command)
      case (state: State, command: GetState) =>
        tell(state, command)
      case (_, _) =>
        other(state, command)
    }
  }

  private def open(state: UninitializedState, command: Open) : ReplyEffect[Event, State] = {
    val opened = Opened(state.status.marketId, command.fixture, command.odds)
    Effect
      .persist(opened)
      .thenReply(command.replyTo)(state => Accepted)
  }

  private def update(state: OpenState, command: Update) : ReplyEffect[Event, State] = {
    val updated = Updated(state.status.marketId, command.odds, command.result)
    Effect
      .persist(updated)
      .thenReply(command.replyTo)(state => Accepted)
  }

  private def close(state: State, command: Close) : ReplyEffect[Event, State] = {
    val closed = Closed(state.status.marketId, OffsetDateTime.now(ZoneId.of("UTC")), state.status.result)
    Effect
      .persist(closed)
      .thenReply(command.replyTo)(state => Accepted)
  }

  private def cancel(state: State, command: Cancel) : ReplyEffect[Event, State] = {
    val cancelled = Cancelled(state.status.marketId, command.reason)
    Effect
      .persist(cancelled)
      .thenReply(command.replyTo)(state => Accepted)
  }

  private def tell(state: State, command: GetState) : ReplyEffect[Event, State] = {
    Effect.
      none.thenReply(command.replyTo)(state => CurrentState(state.status))
  }

  private def other(state: State, command: Command): ReplyEffect[Event, State] = {
    Effect
      .none
      .thenReply(command.replyTo)(_ =>
        RequestUnaccepted(s"[$command] is not allowed upon state [$state]"))
  }

  private def eventHandler(state: State, event: Event) : State = {
    (state, event) match {
      case (_, Opened(marketId, fixture, odds)) =>
        OpenState(Status(marketId, fixture, odds, 0))
      case (state: OpenState, Updated(_, odds, result)) =>
        state.copy(status = Status(
          state.status.marketId,
          state.status.fixture,
          odds.getOrElse(state.status.odds),
          result.getOrElse(state.status.result)
        ))
      case (state: OpenState, Closed(_, _, result)) =>
        ClosedState(state.status.copy(result = result))
      case (state: OpenState, Cancelled(_, _)) =>
        CancelledState(state.status)
    }
  }

}
