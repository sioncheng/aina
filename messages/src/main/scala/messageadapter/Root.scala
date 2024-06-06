package messageadapter

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors

object Root {
  sealed trait Command
  case class Start() extends Command

  def apply(): Behavior[Command] = Behaviors.receive { (ctx, msg) =>
    msg match {
      case Start() =>
        ctx.log.info(s"R1#start")
        Behaviors.same
    }
  }
}
