import akka.NotUsed
import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import akka.persistence.Persistence
import akka.persistence.jdbc.query.scaladsl.JdbcReadJournal
import akka.persistence.query.{EventEnvelope, Offset, PersistenceQuery, Sequence}
import akka.stream.scaladsl.{Keep, Sink, Source}
import akka.stream.testkit.TestSubscriber
import akka.stream.testkit.scaladsl.TestSink
import example.persistence.SPContainer
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class PersistenceQuerySpec
  extends ScalaTestWithActorTestKit
  with AnyWordSpecLike
  with Matchers {

  "a persistence query" should {
    "retrieve the persistenceIds from db and printing them" in {

      val readJournal: JdbcReadJournal = PersistenceQuery(system)
        .readJournalFor[JdbcReadJournal](JdbcReadJournal.Identifier)

      val source: Source[String, NotUsed] = readJournal.persistenceIds()

      source.runForeach(println)

      Thread.sleep(1000)
    }
  }

  "a persistence query" should {
    "retrieve the events from db and printing them" in {
      val readJournal: JdbcReadJournal = PersistenceQuery(system)
        .readJournalFor[JdbcReadJournal](JdbcReadJournal.Identifier)

      val source: Source[EventEnvelope, NotUsed] =
        readJournal.eventsByTag("container-tag-0", Offset.noOffset)

      source.runForeach(each => println(each.event))
      Thread.sleep(1000)
    }
  }

  "a persistence query" should {
    "retrieve the data from db" in {
      val readJournal: JdbcReadJournal = PersistenceQuery(system)
        .readJournalFor[JdbcReadJournal](JdbcReadJournal.Identifier)

      val source: Source[String, NotUsed] = readJournal.persistenceIds()

      val consumer: Sink[String, TestSubscriber.Probe[String]] = TestSink[String]()

      val probe: TestSubscriber.Probe[String] =
        source.toMat(consumer)(Keep.right).run()

      val s = probe.expectSubscription()
      s.request(3)
      probe.expectNext("spcontainer-type-key|9")
    }
  }

  //Bear in mind that this test depends on the order
  //you entered the items when following chapter09b
  "a persistence query" should {
    "retrieve the data from db by tag" in {

      val readJournal = PersistenceQuery(system)
        .readJournalFor[JdbcReadJournal](JdbcReadJournal.Identifier)

      val source: Source[EventEnvelope, NotUsed] =
        readJournal.eventsByTag("container-tag-0", Offset.noOffset)

      val consumer: Sink[EventEnvelope, TestSubscriber.Probe[EventEnvelope]] =
        TestSink[EventEnvelope]()

      val probe: TestSubscriber.Probe[EventEnvelope] =
        source.toMat(consumer)(Keep.right).run()

      val s = probe.expectSubscription()
      s.request(2)

      probe.expectNext(EventEnvelope(Sequence(1),
        "spcontainer-type-key|9",
        1L,
        SPContainer.CargoAdded("9", SPContainer.Cargo("456", "sack", 22)))
      )

      probe.expectNext(EventEnvelope(Sequence(2),
        "spcontainer-type-key|9",
        2L,
        SPContainer.CargoAdded("9", SPContainer.Cargo("457", "bigbag", 15)))
      )
    }
  }
}
