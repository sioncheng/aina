package receptionist

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatest.matchers.should.Matchers
import akka.actor.testkit.typed.scaladsl.LogCapturing
import akka.actor.testkit.typed.scaladsl.LoggingTestKit
import akka.actor.testkit.typed.scaladsl.TestProbe
import akka.actor.typed.ActorRef

class ReceptionistUsageSpec 
    extends ScalaTestWithActorTestKit 
    with AnyWordSpecLike
    with Matchers
    with LogCapturing{

    "An actor subscribed to a ServiceKey" should {
        val guest = spawn(VIPGuest(), "Mr.Wick")

        "get notified about all actors each time an actor registers" in {
            spawn(HotelConcierge())
            LoggingTestKit.info("Mr.Wick is in").expect {
                guest ! VIPGuest.EnterHotel
            }

            val guest2 = spawn(VIPGuest(), "Mr.Ious")
            LoggingTestKit.info("Mr.Ious is in").expect {
                LoggingTestKit.info("Mr.Wick is in").expect {
                    guest2 ! VIPGuest.EnterHotel
                }
            }
        }

        "find that the actor is registered, with search params in Find" in {
            val probe = TestProbe[ActorRef[VIPGuest.Command]]()
            val finder = spawn(GuestFinder(), "finder")
            finder ! GuestFinder.Find("Mr.Wick", probe.ref)
            probe.expectMessageType[ActorRef[VIPGuest.Command]]
        }
    }
}
