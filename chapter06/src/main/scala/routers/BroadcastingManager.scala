package routers

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.scaladsl.PoolRouter
import akka.actor.typed.scaladsl.Routers
import akka.actor.typed.ActorRef

object BroadcastingManager {
  def apply(behavior: Behavior[String]) = Behaviors.setup[Unit] {
    ctx =>
        val poolSize = 4
        val routingBehavior: PoolRouter[String] =
            Routers.pool(poolSize = poolSize)(behavior).withBroadcastPredicate(msg => msg.length() > 0)
        val router: ActorRef[String] = ctx.spawn(routingBehavior, "test-pool")

        (0 to 10).foreach { n =>
            router ! "hi"
        }

        Behaviors.empty
  }
}
