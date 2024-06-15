package example.persistence

import akka.actor.typed.{ActorRef, ActorSystem}
import akka.actor.typed.scaladsl.Behaviors
import akka.cluster.sharding.typed.ShardingEnvelope
import akka.cluster.sharding.typed.scaladsl.{ClusterSharding, Entity}
import org.slf4j.LoggerFactory

import scala.annotation.tailrec
import scala.io.StdIn
import scala.util.control.NonFatal

object Main {

  val logger = LoggerFactory.getLogger(Main.getClass)

  def main(args: Array[String]): Unit = {
    logger.info(s"######## main ########")
    val system = ActorSystem[Nothing](Behaviors.empty, "containers")

    try {
      val shardRegion = init(system)
      commandLoop(system, shardRegion)
    } catch {
      case NonFatal(ex) =>
        logger.error(s"terminated by nonfatal exception", ex)
        system.terminate()
    }
  }

  private def init(system: ActorSystem[_]) :
    ActorRef[ShardingEnvelope[SPContainer.Command]]  = {
    val sharding = ClusterSharding(system)
    val entityDef = Entity(SPContainer.typeKey)(createBehavior = entityCtx => SPContainer(entityCtx.entityId))
    val shardRegion: ActorRef[ShardingEnvelope[SPContainer.Command]] =
      sharding.init(entityDef)
    shardRegion
  }

  @tailrec
  private def commandLoop(system: ActorSystem[_],
                          shardRegion: ActorRef[ShardingEnvelope[SPContainer.Command]]) : Unit = {
    println("############# please write:")
    val commandString = StdIn.readLine()
    println(s"########### inputted $commandString")

    if (commandString == null) {
      system.terminate()
    } else {
      CommandLine.Command(commandString) match {
        case CommandLine.Command.AddCargo(
          containerId,
          cargoId,
          cargoKind,
          cargoSize) =>
          val addCargo = SPContainer.AddCargo(SPContainer.Cargo(cargoId, cargoKind, cargoSize))
          shardRegion ! ShardingEnvelope(containerId, addCargo)
          logger.info(s"######### add cargo $addCargo")
          commandLoop(system, shardRegion)
        case CommandLine.Command.Unknown(consoleInput) =>
          logger.warn("######### unknown command {}", consoleInput)
          commandLoop(system, shardRegion)
        case CommandLine.Command.Quit =>
          logger.info("######## Terminating by user signal")
          system.terminate()
      }
    }
  }
}
