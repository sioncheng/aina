package example

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.kafka.ConsumerMessage.CommittableMessage
import akka.kafka.scaladsl.{ Consumer, Producer }
import akka.kafka.{
  ConsumerSettings,
  ProducerMessage,
  ProducerSettings,
  Subscriptions
}
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.{
  StringDeserializer,
  StringSerializer
}

import scala.concurrent.ExecutionContext

object ConsumerProducerAtLeastOnce {

  implicit val system = ActorSystem(Behaviors.empty, "produceOne")
  implicit val ec = ExecutionContext.Implicits.global

  def main(args: Array[String]): Unit = {
//    println(s"$ec")
//    println(s"${system.executionContext}")
//    val b = ec == system.executionContext
//    println(s"$b")

    val bootstrapServers = "mbp2011:9092"

    val consumerConfig =
      system.settings.config.getConfig("akka.kafka.consumer")

    val consumerSettings = ConsumerSettings(
      consumerConfig,
      new StringDeserializer(),
      new StringDeserializer())
      .withBootstrapServers(bootstrapServers)
      .withGroupId("group03")
      .withProperty(
        ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,
        "earliest")

    val producerConfig =
      system.settings.config.getConfig("akka.kafka.producer")
    val producerSettings = ProducerSettings(
      producerConfig,
      new StringSerializer(),
      new StringSerializer()).withBootstrapServers(bootstrapServers)

    val drainingControl: Consumer.DrainingControl[_] =
      Consumer
        .committableSource(
          consumerSettings,
          Subscriptions.topics("test"))
        .map { msg: CommittableMessage[String, String] =>
          println(s"## msg -> ${msg.record.value()}")
          ProducerMessage.single(
            new ProducerRecord[String, String](
              "test-test",
              msg.record.key(),
              msg.record.value()),
            msg.committableOffset)
        }
        .toMat(Producer.committableSink(producerSettings))(
          Consumer.DrainingControl.apply)
        .run()

    scala.io.StdIn.readLine("Consumer started \n Press Enter to stop")
  }

}
