
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import com.typesafe.config.ConfigFactory
import akka.management.scaladsl.AkkaManagement

object Main extends App {
  val guardian = ActorSystem(Behaviors.empty, "words")
  AkkaManagement(guardian).start()
  scala.io.StdIn.readLine()
  guardian.terminate()
}