import akka.actor.typed.{Behavior, SupervisorStrategy}
import akka.cluster.sharding.typed.scaladsl.EntityTypeKey
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.{Effect, EventSourcedBehavior, ReplyEffect}
import org.slf4j.LoggerFactory
import protocol.WalletProtocol._

import scala.concurrent.duration.DurationInt

object Wallet {

  private val logger = LoggerFactory.getLogger(Wallet + "")

  val TypeKey = EntityTypeKey[Command]("wallet")

  def apply(walletId: String) : Behavior[Command] =
    EventSourcedBehavior[Command, Event, State](
      PersistenceId(TypeKey.name, walletId),
      State(walletId, 0),
      commandHandler = commandHandler,
      eventHandler = eventHandler
    ).onPersistFailure(
      SupervisorStrategy.restartWithBackoff(
        minBackoff = 10.seconds,
        maxBackoff = 60.seconds,
        randomFactor = 0.1
      )
    )

  private def commandHandler(state: State, command: Command) : ReplyEffect[Event, State] = {
    command match {
      case ReserveFunds(amount, replyTo) =>
        logger.debug(s"## reserve funds {} {}", amount, state.walletId)
        if (amount < state.balance)
          Effect
            .persist(FundsReserved(amount))
            .thenReply(replyTo)(state => Accepted)
        else
          Effect
            .persist(FundsReservationDenied(amount))
            .thenReply(replyTo)(state => Rejected)
      case AddFunds(amount, replyTo) =>
        Effect
          .persist(FundsAdded(amount))
          .thenReply(replyTo)(_ => Accepted)
      case CheckFunds(replyTo) =>
        Effect.reply(replyTo)(CurrentBalance(state.balance))
    }
  }

  private def eventHandler(state: State, event: Event): State = {
    event match {
      case FundsReserved(amount) =>
        State(state.walletId, state.balance - amount)
      case FundsAdded(amount) =>
        State(state.walletId, state.balance + amount)
      case FundsReservationDenied(_) =>
        state
    }

  }
}
