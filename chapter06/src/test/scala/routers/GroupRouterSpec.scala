package routers

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatest.matchers.should.Matchers
import akka.actor.testkit.typed.scaladsl.LogCapturing
import akka.actor.testkit.typed.scaladsl.TestProbe
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.receptionist.Receptionist

class GroupRouterSpec 
  extends ScalaTestWithActorTestKit
  with AnyWordSpecLike
  with Matchers
  with LogCapturing {
  
    "a group router" should {
        "send message to one worker registered at a key" ignore {

            val probe1 = TestProbe[String]
            val behavior1 = Behaviors.monitor(probe1.ref, Behaviors.empty[String])

            system.receptionist ! Receptionist.Register(PhotoProcessor.Key, spawn(behavior1))

            val groupRouter = spawn(Camera())
            groupRouter ! Camera.Photo("hi")

            probe1.expectMessage("hi")
            //probe1.receiveMessages(1)
        }

        "send messages to all photo processors registered. With no guarantee of fair distribution." in {
            val photoProcessor1 = TestProbe[String]
            val pp1Monitor = Behaviors.monitor(photoProcessor1.ref, PhotoProcessor())

            val photoProcessor2 = TestProbe[String]
            val pp2Monitor = Behaviors.monitor(photoProcessor2.ref, PhotoProcessor())

            system.receptionist ! Receptionist.Register(PhotoProcessor.Key, spawn(pp1Monitor))
            system.receptionist ! Receptionist.Register(PhotoProcessor.Key, spawn(pp2Monitor))

            val camera = spawn(Camera())
            camera ! Camera.Photo("A")
            camera ! Camera.Photo("B")

            photoProcessor1.receiveMessages(1)
            photoProcessor2.receiveMessages(1)
        }
    }
}
