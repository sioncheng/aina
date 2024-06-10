package example.countwords

import com.typesafe.config.ConfigFactory
import akka.actor.typed.ActorSystem
import akka.actor.typed.Behavior
import akka.cluster.typed.SelfUp
import akka.cluster.ClusterEvent.ClusterDomainEvent
import akka.actor.typed.scaladsl.Behaviors
import akka.cluster.typed.Cluster
import akka.cluster.typed.Subscribe
import akka.actor.typed.scaladsl.Routers

object CountWordsApp {
  def main(args: Array[String]): Unit = {
    if (args.isEmpty) {
        startup("worker", 0)
    } else {
        require(args.size == 2, "Usage: two parameters required 'role' and 'port'")
        startup(args(0), args(1).toInt)
    }
  }

  def startup(role: String, port: Int): Unit = {
    val config = ConfigFactory
        .parseString(s"""
            akka.remote.artery.canonical.port=$port
            akka.cluster.roles = [$role]
        """)
        .withFallback(ConfigFactory.load("words"))
    
    val guardian = ActorSystem(ClusterGuardian(), "WordsCluster", config)

    println(s"############## startup as $role ##############")
    println("############## press ENTER to terminate ##############")
    scala.io.StdIn.readLine()
    guardian.terminate()
  }

  private object ClusterGuardian {
    def apply(): Behavior[ClusterDomainEvent] = 
      Behaviors.setup[ClusterDomainEvent] { ctx =>
        val cluster = Cluster(ctx.system)
        if (cluster.selfMember.hasRole("director")) {
          cluster.subscriptions ! Subscribe(ctx.self, classOf[SelfUp])
        }
        if (cluster.selfMember.hasRole("aggregator")) {
          val numberOfWorkers = 
            ctx.system.settings.config.getInt("example.countwords.workers-per-node")
          for (i <- 0 to numberOfWorkers) {
            ctx.spawn(Worker(), s"workder-$i")
          }
        }

        Behaviors.receiveMessage { eve =>
          ctx.log.debug(s"############## director $eve ##############")
          eve match {
            case SelfUp(currentClusterState) =>
              ctx.log.info("selfUp $currentClusterState")
              val router = ctx.spawnAnonymous(Routers.group(WorkerProtocol.RegistrationKey))
              ctx.spawn(Master(router), "master")
              Behaviors.same
            }
        }
      }
  }
}
