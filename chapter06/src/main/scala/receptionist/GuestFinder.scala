package receptionist

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.ActorRef
import akka.actor.typed.receptionist.{Receptionist, ServiceKey}
import akka.util.Timeout
import scala.util.{Failure, Success}
import scala.concurrent.duration._

object GuestFinder {

    sealed trait Command
    final case class Find(actorName: String,
        replyTo: ActorRef[ActorRef[VIPGuest.Command]])
        extends Command
    
    final case object Void extends Command

    def apply() = Behaviors.setup[Command] { context =>
        implicit val timeout: Timeout = 3.seconds
        Behaviors.receiveMessage {
            case Find(actorName, replyTo) =>
                //context.log.info(s"find ${actorName}")
                context.ask(context.system.receptionist, Receptionist.Find(HotelConcierge.GoldenKey)) {
                    case Success(HotelConcierge.GoldenKey.Listing(listings)) =>
                        context.log.info(s"success listing's size ${listings.size}")
                        listings
                            .filter(_.path.name.contains(actorName))
                            .foreach(actor => replyTo ! actor)
                        Void
                    case Failure(ex) =>
                        context.log.error(ex.getMessage())
                        Void
                    case Success(value) => 
                        context.log.info(s"success value ${value}")
                        Void
                }
                Behaviors.same
            
            case Void =>
                //context.log.info(s"void then empty")
                Behaviors.same
        }
    }
}