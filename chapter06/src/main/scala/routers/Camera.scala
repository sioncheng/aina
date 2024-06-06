package routers

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.scaladsl.GroupRouter
import akka.actor.typed.scaladsl.Routers
import akka.actor.typed.ActorRef

object Camera {
  final case class Photo(content: String)

  def apply() = Behaviors.setup[Photo] { ctx => 
        val routingBehavior: GroupRouter[String] =
            Routers.group(PhotoProcessor.Key).withRoundRobinRouting()
        val router : ActorRef[String] = ctx.spawn(routingBehavior, "photo-processor-pool")
        
        Behaviors.receiveMessage {
            case Photo(content) =>
                router ! content
                Behaviors.same
        }

    }
}
