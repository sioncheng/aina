package messageadapter

import org.slf4j.LoggerFactory
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.ActorRef
import akka.actor.Actor
import akka.actor.typed.ActorSystem

object F1 {

  sealed trait Command
  case class Hello(who: String) extends Command
  case class BackResponse(res: BackendActor.Response) extends Command

  def apply(backRef: ActorRef[BackendActor.BackMessage])
      : Behavior[Command] =
    Behaviors.setup { ctx =>

      val adapter = ctx.messageAdapter(BackResponse)

      Behaviors.receive { (ctx, msg) =>
        msg match {
          case Hello(who) =>
            backRef ! BackendActor.MakeHello(who, adapter)
            Behaviors.same
          case BackResponse(res) =>
            //ctx.log.info(s"back response ${res}")
            res match {
              case BackendActor.Hello(m) =>
                ctx.log.info(s"back response ${m}")
            }
            Behaviors.same
        }
      }

    }
}

object MessageAdapterApp extends App {
  private val logger = LoggerFactory.getLogger("MessageAdapterApp")
  logger.info(s"message adapter app")

  val root: ActorSystem[Root.Command] = ActorSystem(Root(), "root")
  //root ! R1.Start()

  val b1: ActorRef[BackendActor.BackMessage] =
    root.systemActorOf(BackendActor(), "b1")
  val f1: ActorRef[F1.Command] = root.systemActorOf(F1(b1), "f1")

  f1 ! F1.Hello("messageAdapterApp")

  scala.io.StdIn.readLine()

  root.terminate()
}
