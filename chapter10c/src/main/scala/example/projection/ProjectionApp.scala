package example.projection

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.cluster.sharding.typed.ShardedDaemonProcessSettings
import akka.cluster.sharding.typed.scaladsl.ShardedDaemonProcess
import akka.projection.ProjectionBehavior
import com.typesafe.config.ConfigFactory
import example.repository.scalike.{CargosPerContainerRepositoryImpl, ScalikeJdbcSetup}
import org.slf4j.LoggerFactory

import scala.io.StdIn
import scala.util.control.NonFatal

object ProjectionApp {

  final private val logger = LoggerFactory.getLogger(ProjectionApp + "")

  def main(args: Array[String]): Unit = {
    logger.info("initializing system")
    val port = if (args.isEmpty) {25521} else {args(0).toInt}
    val system = initActorSystem(port)

    logger.info("initializing projection")
    try {
      ScalikeJdbcSetup.init(system)
      initProjection(system)
    } catch {
      case NonFatal(ex) =>
        logger.error(s"terminating by NonFatal Exception", ex)
        system.terminate()
    }

    StdIn.readLine()
    logger.info("###### exit #######")
    system.terminate()
  }

  private def initActorSystem(port: Int) : ActorSystem[Nothing] = {
    val config = ConfigFactory
      .parseString(
        s"""
           akka.remote.artery.canonical.port=$port
           """)
      .withFallback(ConfigFactory.load())

    ActorSystem[Nothing](Behaviors.empty, "containersprojection", config)
  }

  private def initProjection(system: ActorSystem[Nothing]): Unit = {
    ShardedDaemonProcess(system).init(
      name = "cargos-per-container-projection",
      numberOfInstances = 3,
      behaviorFactory = index =>
        ProjectionBehavior(
          CargosPerContainerProjection.createProjectionFor(
            system,
            new CargosPerContainerRepositoryImpl(),
            index
          )
        ),
      settings = ShardedDaemonProcessSettings(system),
      stopMessage = Some(ProjectionBehavior.Stop)
    )
  }
}
