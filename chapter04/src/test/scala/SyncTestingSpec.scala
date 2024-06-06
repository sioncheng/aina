import akka.actor.testkit.typed.scaladsl.BehaviorTestKit
import akka.actor.testkit.typed.scaladsl.TestInbox
import akka.actor.testkit.typed.Effect.{
  NoEffects,
  Scheduled,
  Spawned
}
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

class SyncTestingSpec extends AnyWordSpec with Matchers {
  "Typed actor synchronous testing" must {
    "spawning takes place" in {
      val testKit = BehaviorTestKit(SimplifiedManager())
      testKit.expectEffect(NoEffects)
      testKit.run(SimplifiedManager.CreateChild("adan"))
      testKit.expectEffect(Spawned(SimplifiedWorker(), "adan"))
    }
    "actor gets forwarded message from manager" in {
      val testKit = BehaviorTestKit(SimplifiedManager())
      val probe = TestInbox[String]()
      testKit.run(SimplifiedManager.Forward("hello", probe.ref))
      probe.expectMessage("hello")
    }
  }
}
