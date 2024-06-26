package protocol

import akka.actor.typed.ActorRef

import java.time.OffsetDateTime

object MarketProtocol {

  final case class Fixture(id: String, homeTeam: String, awayTeam: String)
    extends CborSerializable
  final case class Odds(winHome: Double, winAway: Double, draw: Double)
    extends CborSerializable

  final case class Status(marketId: String,
                          fixture: Fixture,
                          odds: Odds,
                          result: Int) extends CborSerializable
  object Status {
    def empty(marketId: String): Status =
      Status(marketId, Fixture("", "", ""), Odds(-1, -1, -1), 0)
  }

  sealed trait Response extends CborSerializable
  final case object Accepted extends Response
  final case class CurrentState(status: Status) extends Response
  final case class RequestUnaccepted(reason: String) extends Response

  sealed trait Command extends CborSerializable {
    def replyTo: ActorRef[Response]
  }
  final case class Open(
      fixture: Fixture,
      odds: Odds,
      opensAt: OffsetDateTime,
      replyTo: ActorRef[Response]) extends Command
  final case class Update(
      odds: Option[Odds],
      opensAt: Option[OffsetDateTime],
      result: Option[Int], //1 = winHome, 2 = winAway, 0 = draw
      replyTo: ActorRef[Response]) extends Command
  final case class Close(replyTo: ActorRef[Response]) extends Command
  final case class Cancel(reason: String, replyTo: ActorRef[Response]) extends Command
  final case class GetState(replyTo: ActorRef[Response]) extends Command

  sealed trait State extends CborSerializable {
    def status : Status
  }
  final case class UninitializedState(status: Status) extends State
  final case class OpenState(status: Status) extends State
  final case class ClosedState(status: Status) extends State
  final case class CancelledState(status: Status) extends State

  sealed trait Event extends CborSerializable {
    def marketId: String
  }
  final case class Opened(marketId: String, fixture: Fixture, odds: Odds) extends Event
  final case class Updated(marketId: String, odds: Option[Odds], result: Option[Int]) extends Event
  final case class Closed(marketId: String, at: OffsetDateTime, result: Int) extends Event
  final case class Cancelled(marketId: String, reason: String) extends Event
}
