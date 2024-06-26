import org.slf4j.LoggerFactory

object App {

  private val logger = LoggerFactory.getLogger(App + "")

  def main(args: Array[String]): Unit = {
    logger.info("## app run")
  }
}
