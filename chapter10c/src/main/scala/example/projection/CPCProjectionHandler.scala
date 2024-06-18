package example.projection

import akka.projection.eventsourced.EventEnvelope
import akka.projection.jdbc.scaladsl.JdbcHandler
import example.persistence.SPContainer
import example.repository.scalike.{CargosPerContainerRepository, ScalikeJdbcSession}
import org.slf4j.{Logger, LoggerFactory}

class CPCProjectionHandler (repository: CargosPerContainerRepository)
  extends JdbcHandler[EventEnvelope[SPContainer.Event], ScalikeJdbcSession]{

  final private val logger: Logger = LoggerFactory.getLogger(classOf[CPCProjectionHandler])

  override def process(session: ScalikeJdbcSession, envelope: EventEnvelope[SPContainer.Event]): Unit = {
    envelope.event match {
      case SPContainer.CargoAdded(containerId, cargo) =>
        repository.addCargo(containerId, session)
        logger.debug("**** cargo added {} in projection", cargo)
      case x =>
        logger.debug("**** ignoring event {} in projection", x)
    }
  }
}
