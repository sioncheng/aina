package example.projection

import akka.actor.typed.ActorSystem
import akka.persistence.jdbc.query.scaladsl.JdbcReadJournal
import akka.persistence.query.Offset
import akka.projection.ProjectionId
import akka.projection.eventsourced.EventEnvelope
import akka.projection.eventsourced.scaladsl.EventSourcedProvider
import akka.projection.jdbc.scaladsl.JdbcProjection
import akka.projection.scaladsl.ExactlyOnceProjection
import example.persistence.SPContainer
import example.repository.scalike.{CargosPerContainerRepository, ScalikeJdbcSession}
import org.slf4j.{Logger, LoggerFactory}

object CargosPerContainerProjection {

  final private val logger: Logger = LoggerFactory.getLogger(CargosPerContainerProjection + "ß")

  def createProjectionFor(system: ActorSystem[_],
                          repository: CargosPerContainerRepository,
                          indexTag: Int): ExactlyOnceProjection[Offset, EventEnvelope[SPContainer.Event]] = {

    val tag = "container-tag-" + indexTag

    val sourceProvider = EventSourcedProvider.eventsByTag[SPContainer.Event] (
      system = system,
      readJournalPluginId = JdbcReadJournal.Identifier,
      tag = tag
    )

    JdbcProjection.exactlyOnce(
      projectionId = ProjectionId("CargosPerContainerProjection", tag),
      sourceProvider = sourceProvider,
      handler = () => new CPCProjectionHandler(repository),
      sessionFactory = () => new ScalikeJdbcSession()
    )(system)
  }
}
