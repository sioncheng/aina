package example

import akka.Done
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.kafka.ProducerSettings
import akka.kafka.scaladsl.Producer
import akka.stream.scaladsl.Source
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.{
  StringDeserializer,
  StringSerializer
}

import scala.concurrent.Future

object ProducerPlainSource {

  def main(args: Array[String]): Unit = {

    implicit val system = ActorSystem(Behaviors.empty, "producerOne")

    val config =
      system.settings.config.getConfig("akka.kafka.producer")

    val topic = "test"

    val producerSettings = ProducerSettings(
      config,
      new StringSerializer(),
      new StringSerializer()).withBootstrapServers("mbp2011:9092")

    val done: Future[Done] = Source(1 to 10)
      .map(_.toString)
      .map(s => new ProducerRecord[String, String](topic, "m" + s))
      .runWith(Producer.plainSink(producerSettings))

    scala.io.StdIn.readLine("producer started \n Press ENTER to stop")

    //implicit val ec = system.executionContext
    done.onComplete { _ => system.terminate() }(
      system.executionContext)
  }
}
