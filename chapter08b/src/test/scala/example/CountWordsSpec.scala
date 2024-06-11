package example

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatest.matchers.should.Matchers
import akka.actor.testkit.typed.scaladsl.LogCapturing
import example.countwords.Worker
import akka.actor.typed.scaladsl.Routers
import example.countwords.WorkerProtocol
import example.countwords.MasterProtocol
import akka.actor.typed.scaladsl.Behaviors
import example.countwords.Master
import com.typesafe.config.ConfigFactory

class CountWordsSpec 
  extends ScalaTestWithActorTestKit(
    ConfigFactory
      .parseString("""example.countwords.workders-per-node = 5""")
      .withFallback(ConfigFactory.load("words"))
  )
  with AnyWordSpecLike
  with Matchers
  with LogCapturing {
  
  "The words app" should {
    "send work from the master to the workers and back" in {
      val numberOfWorkers = 
        system.settings.config.getInt("example.countwords.workders-per-node")
      
      for (i <- 0 to numberOfWorkers) {
        spawn(Worker(), s"workder-$i")
      }

      println("======== spawn workers ========")

      val router = spawn {Routers.group(WorkerProtocol.RegistrationKey)}
      val probe = createTestProbe[MasterProtocol.Event]
      val masterMonitered = Behaviors.monitor(probe.ref, Master(router))
      spawn(masterMonitered, "master0")

      println("======== spawn master ========")


      probe.expectMessage(MasterProtocol.Tick)
      probe.expectMessage(
        MasterProtocol.CountedWords(
          Map(
            "this" -> 1,
            "yes" -> 1,
            "a" -> 1,
            "really" -> 1,
            "very" -> 2,
            "simulates" -> 1,
            "simple" -> 2,
            "stream" -> 1)))

    }
  }
}
