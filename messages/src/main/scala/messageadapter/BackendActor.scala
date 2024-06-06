package messageadapter

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors

object BackendActor {
  sealed trait Response
  case class Hello(msg: String) extends Response

  sealed trait BackMessage
  case class MakeHello(who: String, replyTo: ActorRef[Response])
      extends BackMessage

  def apply(): Behavior[BackMessage] = Behaviors.receive {
    (ctx, msg) =>
      msg match {
        case MakeHello(who, replyTo) =>
          ctx.log.info(s"make hello ${who} ${replyTo}")
          replyTo ! Hello(s"hello ${who}")
          Behaviors.same
      }
  }

}
