
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.stream.scaladsl.Source
import akka.persistence.query.PersistenceQuery
import akka.persistence.jdbc.query.scaladsl.JdbcReadJournal
import akka.NotUsed

object Main extends App {

  implicit val system : ActorSystem[Any] = ActorSystem(Behaviors.ignore, "persistence-query")

  val readJournal: JdbcReadJournal = PersistenceQuery(system)
    .readJournalFor[JdbcReadJournal](JdbcReadJournal.Identifier)

  val source: Source[String, NotUsed] = readJournal.persistenceIds

  source.runForeach(println)

  scala.io.StdIn.readLine()

  system.terminate()
}