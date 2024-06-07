import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import com.typesafe.config.ConfigFactory
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatest.matchers.should.Matchers
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.Behavior
import akka.actor.testkit.typed.scaladsl.LoggingTestKit

class AsyncLogConfigSpec 
  extends ScalaTestWithActorTestKit(
    ConfigFactory.parseString("""akka.eventsourced-entity.journal-enabled = false""")
      .withFallback(ConfigFactory.load("in-memory"))
  )
  with AnyWordSpecLike
  with Matchers {

    "Actor" must {
      "log in debug the content when receive message" in {
        val loggerBehavior: Behavior[String] = Behaviors.receive { (ctx, msg) =>
          msg match {
            case message: String =>
              ctx.log.debug(s"message $message received")
              Behaviors.same
          }
        }

        val loggerActor = spawn(loggerBehavior)
        val message = "hi"

        LoggingTestKit.debug(s"message $message received").expect {
          loggerActor.ref ! message
        }
      }

      "lift one property form conf" in {
        val inmemory = testKit.system.settings.config
        val journalEnabled = inmemory.getString("akka.eventsourced-entity.journal-enabled")
        val readJournal = inmemory.getString("akka.eventsourced-entity.read-journal")

        val loggerBehavior: Behavior[String] = Behaviors.receive { (ctx, msg) =>
          msg match {
            case message: String =>
              ctx.log.info(s"$journalEnabled $readJournal")
              Behaviors.same
          }
        }

        val loggerActor = spawn(loggerBehavior)
        val message = "anymessage"
        
        LoggingTestKit.info("false inmem-read-journal")
        .expect {
          loggerActor.ref ! message
        }
      }
    }
}
