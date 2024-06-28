package example

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.kafka.{ ConsumerSettings, Subscriptions }
import akka.kafka.scaladsl.Consumer
import akka.stream.scaladsl.Sink
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.serialization.StringDeserializer

object ConsumerPlainSource {

  def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem(Behaviors.empty, "consumerOne")

    val config =
      system.settings.config.getConfig("akka.kafka.consumer")

    val consumerSettings: ConsumerSettings[String, String] =
      ConsumerSettings(
        config,
        new StringDeserializer(),
        new StringDeserializer())
        .withBootstrapServers("mbp2011:9092")
        .withGroupId("group01")

    Consumer
      .plainSource(consumerSettings, Subscriptions.topics("test"))
      .map { msg: ConsumerRecord[String, String] =>
        println(s"## key = ${msg.key()} value = ${msg
          .value()} offset = ${msg.offset()}")
      }
      .runWith(Sink.ignore)

    scala.io.StdIn.readLine("Consumer started \n Press ENTER to stop")
    system.terminate()
  }

}
