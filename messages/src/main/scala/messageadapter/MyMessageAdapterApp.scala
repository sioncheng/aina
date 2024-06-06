package messageadapter

import akka.actor.typed.ActorSystem
import akka.actor.typed.Behavior
import akka.actor.typed.ActorRef
import akka.actor.typed.scaladsl.Behaviors
import org.slf4j.LoggerFactory

object MessageAdapter {

  def apply(f2: ActorRef[F2.Command]): Behavior[Object] =
    Behaviors.receive { (ctx, msg) =>
      {
        msg match {
          case BackendActor.Hello(mm) => {
            ctx.log.info(s"forward to f2 ${msg}")
            f2 ! F2.BackResponse(BackendActor.Hello(mm))
          }
        }

        Behaviors.same
      }
    }
}

object F2 {

  sealed trait Command
  case class Hello(who: String) extends Command
  case class BackResponse(res: BackendActor.Response) extends Command

  def apply(backend: ActorRef[BackendActor.BackMessage])
      : Behavior[Command] =
    Behaviors.setup { ctx =>
      {
        val ma: Behavior[Object] = MessageAdapter(ctx.self)
        val adapter: ActorRef[BackendActor.Response] =
          ctx.spawnAnonymous(ma)
        Behaviors.receive { (ctx, msg) =>
          {
            msg match {
              case Hello(who) =>
                backend ! BackendActor.MakeHello(who, adapter)
                Behaviors.same
              case BackResponse(res) =>
                ctx.log.info(s"back response ${res}")
                Behaviors.same
            }
          }
        }
      }
    }
}

object MyMessageAdapterApp extends App {
  private val logger = LoggerFactory.getLogger("MyMessageAdapterApp")
  logger.info("my message adapter app")

  val root: ActorSystem[Root.Command] = ActorSystem(Root(), "root")
  val backend: ActorRef[BackendActor.BackMessage] =
    root.systemActorOf(BackendActor(), "b1")
  var f2: ActorRef[F2.Command] = root.systemActorOf(F2(backend), "f2")

  f2 ! F2.Hello("myMessageAdapterApp1")
  f2 ! F2.Hello("myMessageAdapterApp2")

  scala.io.StdIn.readLine()
  root.terminate()
}
