package protocol

import akka.actor.typed.ActorRef

object WalletProtocol {

  sealed trait Response extends CborSerializable
  trait UpdatedResponse extends Response
  case object Accepted extends UpdatedResponse
  case object Rejected extends UpdatedResponse
  case class CurrentBalance(amount: Int) extends Response

  sealed trait Command extends CborSerializable

  case class ReserveFunds(
      amount:Int,
      replyTo: ActorRef[UpdatedResponse])
    extends Command

  case class AddFunds(
      amount: Int,
      replyTo: ActorRef[UpdatedResponse])
    extends Command

  case class CheckFunds(replyTo: ActorRef[Response])
    extends Command

  sealed trait Event extends CborSerializable
  case class FundsReserved(amount: Int) extends Event
  case class FundsAdded(amount: Int) extends Event
  case class FundsReservationDenied(amount: Int) extends Event

  case class State(walletId: String, balance: Int) extends CborSerializable
}
