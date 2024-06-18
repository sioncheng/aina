package example.projection

import akka.actor.typed.{ActorSystem, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import akka.cluster.sharding.typed.scaladsl.ShardedDaemonProcess
import com.typesafe.config.ConfigFactory
import org.slf4j.LoggerFactory

import scala.io.StdIn

object LoggerShardedApp {

  final val logger = LoggerFactory.getLogger(LoggerShardedApp + "")

  def main(args: Array[String]) : Unit = {
    startup(args(0).toInt)
  }

  private def startup(port: Int): Unit = {
    logger.info(s"starting cluster on port {}", port)

    val config = ConfigFactory.parseString(
      s"""
        akka.remote.artery.canonical.port=$port
        """)
      .withFallback(ConfigFactory.load("shardeddeamon"))

    val system =
      ActorSystem[Nothing](Behaviors.empty, "LoggerSharded", config)

    val tags = Vector("container-tag-1", "container-tag-2", "container-tag-3")

    ShardedDaemonProcess(system).init(
      "loggers",
      tags.size,
      index => LoggerBehavior(tags(index))
    )

    StdIn.readLine()
    logger.info("###### exit #######")
    system.terminate()
  }

  private def LoggerBehavior(tag: String): Behavior[Unit] = {
    Behaviors.setup {ctx =>
      ctx.log.info("************ spawned LoggerBehavior {} *************", tag)
      Behaviors.ignore
    }
  }
}
