import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.management.cluster.bootstrap.ClusterBootstrap
import akka.management.scaladsl.AkkaManagement
import com.typesafe.config.ConfigFactory

object App {

  def main(args: Array[String]): Unit = {
    val host = args(0)
    val port = args(1)

    val s = s"""
           akka.remote.artery.canonical.hostname = $host
           akka.management.http.hostname = $host
           akka.management.http.port=$port
           """

    val config = ConfigFactory
      .parseString(s)
      .withFallback(ConfigFactory.load())

    val system =
      ActorSystem[Nothing] (Behaviors.empty, "testing-bootstrap", config)

    AkkaManagement(system).start()
    ClusterBootstrap(system).start()

    scala.io.StdIn.readLine()
    system.terminate()
  }
}
