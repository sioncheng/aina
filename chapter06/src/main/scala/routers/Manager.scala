package routers

import akka.actor.typed.Behavior
import akka.actor.typed.ActorRef
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.scaladsl.Routers

object Manager {
  def apply(behavior: Behavior[String]) = Behaviors.setup[Unit] {
    ctx =>
        val routingBehavior: Behavior[String] = 
            Routers.pool(poolSize = 4)(behavior)
        val router: ActorRef[String] = 
            ctx.spawn(routingBehavior, "test-pool")
        
        (0 to 10).foreach { n =>
            router ! s"hi ${n}"
        }

        Behaviors.empty
  }
}
