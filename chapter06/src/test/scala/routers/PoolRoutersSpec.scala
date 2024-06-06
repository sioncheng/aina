package routers

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatest.matchers.should.Matchers
import akka.actor.testkit.typed.scaladsl.LogCapturing
import akka.actor.testkit.typed.scaladsl.TestProbe

class PoolRoutersSpec 
    extends ScalaTestWithActorTestKit
    with AnyWordSpecLike
    with Matchers
    with LogCapturing {

    "a pool router" should {
        "send message in round-robing fashion" in {

            val probe = TestProbe[String]
            val worker = Worker(probe.ref)
            val router = spawn(Manager(worker), "round-robin")


            probe.receiveMessages(10)

        }

        "broadcast, sending each message to all routees" in {
            val probe = TestProbe[String]
            val worker = Worker(probe.ref)

            val router = spawn(BroadcastingManager(worker), "broadcasting")

            //probe.expectMessage("hi")
            probe.receiveMessages(40)
        }
    }
  
}
