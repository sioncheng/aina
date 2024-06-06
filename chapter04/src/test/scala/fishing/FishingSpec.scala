package fishing

import org.scalatest.matchers.must.Matchers
import fishing.CounterTimer
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.testkit.typed.scaladsl.FishingOutcomes
import scala.concurrent.duration.DurationInt
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.BeforeAndAfter
import org.scalatest.BeforeAndAfterAll
import akka.actor.testkit.typed.scaladsl.ActorTestKit

class FishingSpec
    extends AnyWordSpec
    with BeforeAndAfterAll
    with Matchers {

  val testKit = ActorTestKit()

  "An automated resuming counter" must {
    "receive a resume after a pause" in {
      val probe = testKit.createTestProbe[CounterTimer.Command]()
      val counterMonitored =
        Behaviors.monitor(probe.ref, CounterTimer())
      val counter = testKit.spawn(counterMonitored)

      counter ! CounterTimer.Pause(1)

      probe.fishForMessage(3.seconds) {
        case CounterTimer.Increase =>
          FishingOutcomes.continueAndIgnore
        case CounterTimer.Pause(_) =>
          FishingOutcomes.continueAndIgnore
        case CounterTimer.Resume =>
          FishingOutcomes.complete
      }
    }
  }

  override protected def afterAll(): Unit = testKit.shutdownTestKit()

}
