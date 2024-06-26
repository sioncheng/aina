package protocol

import akka.actor.typed.ActorRef

object BetProtocol {

  final case class Status(
                           betId: String,
                           walletId: String,
                           marketId: String,
                           odds: Double,
                           stake: Int,
                           result: Int) extends CborSerializable
  object Status {
    def empty(marketId: String) =
      Status(marketId, "uninitialized", "uninitialized", -1, -1, 0)
  }

  sealed trait State extends CborSerializable {
    def status: Status
  }

  final case class UninitializedState(status: Status) extends State
  final case class OpenState(
      status: Status,
      marketConfirmed: Option[Boolean] = None,
      fundsConfirmed: Option[Boolean] = None) extends State
  final case class SettledState(status: Status) extends State
  final case class CancelledState(status: Status) extends State
  final case class FailedState(status: Status) extends State
  final case class ClosedState(status: Status) extends State

  sealed trait Response
  final case object Accepted extends Response
  final case class RequestUnaccepted(reason: String)
  final case class CurrentState(state: State) extends Response

  sealed trait Command extends CborSerializable
  trait ReplyCommand extends Command {
    def replyTo: ActorRef[Response]
  }

  final case class Open(
      walletId: String,
      marketId: String,
      odds: Double,
      stake: Int,
      result: Int,
      replyTo: ActorRef[Response]) extends ReplyCommand
  final case class Cancel(reason: String, replyTo: ActorRef[Response]) extends Command
  final case class GetState(replyTo: ActorRef[Response]) extends Command

  final case class MarketOddsAvailable(
      available: Boolean,
      marketOdds: Option[Double]) extends Command
  final case class RequestWalletFunds(
      response: WalletProtocol.UpdatedResponse) extends Command
  final case class ValidationTimeout(seconds: Int) extends Command
  final case class Fail(reason: String) extends Command
  final case class Close(reason: String) extends Command
  final case class CheckMarketOdds() extends Command
  final case class Settle() extends Command

  sealed trait Event extends CborSerializable
  final case class MarketConfirmed(state: OpenState) extends Event
  final case class FundsGranted(state: OpenState) extends Event
  final case class ValidationPassed(state: OpenState) extends Event
  final case class Opened(
      betId: String,
      walletId: String,
      marketId: String,
      odds: Double,
      stake: Int,
      result: Int) extends Event
  final case class Settled(betId: String) extends Event
  final case class Cancelled(betId: String, reason: String) extends Event
  final case class Failed(betId: String, reason: String) extends Event
  final case object Closed extends Event
}
