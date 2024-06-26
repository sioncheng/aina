import akka.actor.typed.{ActorRef, Behavior, SupervisorStrategy}
import akka.actor.typed.scaladsl.{ActorContext, Behaviors, TimerScheduler}
import akka.cluster.sharding.typed.scaladsl.{ClusterSharding, EntityTypeKey}
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.{Effect, EventSourcedBehavior}
import akka.util.Timeout
import protocol.BetProtocol._
import protocol.{MarketProtocol, WalletProtocol}

import scala.concurrent.duration.{DurationInt, SECONDS}
import scala.util.{Failure, Success}

object Bet {
  val TypeKey = EntityTypeKey[Command]("bet")

  def apply(betId: String) : Behavior[Command] = {
    Behaviors.withTimers {timer =>
      Behaviors.setup {ctx =>
        val sharding = ClusterSharding(ctx.system)
        EventSourcedBehavior[Command, Event, State] (
          PersistenceId(TypeKey.name, betId),
          UninitializedState(Status.empty(betId)),
          commandHandler = (state, command) => handleCommand(state, command, sharding, ctx, timer),
          eventHandler = (state, event) => handleEvent(state, event)
        ).onPersistFailure(
          SupervisorStrategy.restartWithBackoff(
            minBackoff = 10.seconds,
            maxBackoff = 60.seconds,
            randomFactor = 0.1
          )
        )
      }
    }
  }

  private def handleCommand(
      state: State,
      command: Command,
      sharding: ClusterSharding,
      ctx: ActorContext[Command],
      timer: TimerScheduler[Command]
                   ): Effect[Event, State] = {
    (state, command) match {
      case (state: UninitializedState, command: Open) =>
        open(state, command, sharding, ctx, timer)
      case (state: OpenState, command: CheckMarketOdds) =>
        validateMarket(state, command)
      case (state: OpenState, command: RequestWalletFunds) =>
        validateFunds(state, command)
      case(state: OpenState, command: ValidationTimeout) =>
        checkValidations(state, command)
      case(state: OpenState, command: Close) =>
        close(state, command)
      case (_, command: Cancel) =>
        cancel(state, command)
      case (_, command: ReplyCommand) =>
        reject(state, command)
      case _ =>
        invalid(state, command, ctx)
    }
  }

  private def open(
    state: State,
    command: Open,
    sharding: ClusterSharding,
    context: ActorContext[Command],
    timer: TimerScheduler[Command]): Effect[Event, State] = {

    timer.startSingleTimer("lifespan", ValidationTimeout(10), 10.seconds)
    val opened = Opened(
      state.status.betId,
      command.walletId,
      command.marketId,
      command.odds,
      command.stake,
      command.result
    )
    Effect
      .persist(opened)
      .thenRun((s: State) => requestMarketStatus(command, sharding, context))
      .thenRun((s: State) => requestFundsReservation(command, sharding, context))
      .thenReply(command.replyTo)(s => Accepted)
  }

  private def requestMarketStatus(
    open: Open,
    sharding: ClusterSharding,
    ctx: ActorContext[Command]): Unit = {

    val marketRef = sharding.entityRefFor(Market.TypeKey, open.marketId)
    implicit val timeout: Timeout = Timeout(3, SECONDS)
    ctx.ask(marketRef, MarketProtocol.GetState) {
      case Success(MarketProtocol.CurrentState(marketState)) =>
        val matched = oddsDoMatch(marketState, open)
        MarketOddsAvailable(matched._1, Option(matched._2))
      case Failure(e) =>
        val em = e.getMessage
        ctx.log.error(s"requestMarketStatus failure ${em}")
        MarketOddsAvailable(1 == 2, None)
    }
  }

  private def requestFundsReservation(open: Open, sharding: ClusterSharding, ctx: ActorContext[Command]) : Unit = {
    val walletRef = sharding.entityRefFor(Wallet.TypeKey, open.walletId)
    val walletResponseAdapter =
      ctx.messageAdapter[WalletProtocol.UpdatedResponse](res => RequestWalletFunds(res))
    walletRef ! WalletProtocol.ReserveFunds(open.stake, walletResponseAdapter)
  }

  private def validateMarket(
    state: State,
    command: Command): Effect[Event, State] = ???

  private def validateFunds(
    state: OpenState,
    command: RequestWalletFunds): Effect[Event, State] = {

    command.response match {
      case WalletProtocol.Accepted =>
        Effect.persist(FundsGranted(state))
      case WalletProtocol.Rejected =>
        Effect.persist(Failed(state.status.betId, "funds not available"))
    }
  }

  private def checkValidations(
    state: State,
    command: Command): Effect[Event, State] = ???

  private def close(
    state: State,
    command: Command): Effect[Event, State] = ???

  private def cancel(
    state: State,
    command: Command): Effect[Event, State] = ???

  private def reject(
    state: State,
    command: Command): Effect[Event, State] = ???

  private def invalid(
    state: State,
    command: Command,
    context: ActorContext[Command]): Effect[Event, State] = ???

  private def handleEvent(state: State, event: Event): State = ???

  private def oddsDoMatch(status: MarketProtocol.Status, command: Open): Tuple2[Boolean, Double] = {
    Tuple2(1 == 2, 1)
  }
}
