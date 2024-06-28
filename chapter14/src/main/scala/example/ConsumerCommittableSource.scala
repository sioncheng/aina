package example

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.kafka.ConsumerMessage.CommittableMessage
import akka.kafka.scaladsl.{ Committer, Consumer }
import akka.kafka.scaladsl.Consumer.DrainingControl
import akka.kafka.{
  CommitterSettings,
  ConsumerSettings,
  Subscriptions
}
import akka.stream.scaladsl.Sink
import org.apache.kafka.common.serialization.StringDeserializer

object ConsumerCommittableSource {

  def main(args: Array[String]): Unit = {

    implicit val system = ActorSystem(Behaviors.empty, "consumerOne")

    implicit val ec = system.executionContext

    val config =
      system.settings.config.getConfig("akka.kafka.consumer")

    val consumerSettings: ConsumerSettings[String, String] =
      ConsumerSettings(
        config,
        new StringDeserializer(),
        new StringDeserializer())
        .withBootstrapServers("mbp2011:9092")
        .withGroupId("group02")

    val commiterSettings = CommitterSettings(system)

    val drainingControl: DrainingControl[_] =
      Consumer
        .committableSource(
          consumerSettings,
          Subscriptions.topics("test"))
        .map { msg: CommittableMessage[String, String] =>
          println(s"## ${msg.record.key()} -> ${msg.record.value()}")
          msg.committableOffset
        }
        .via(Committer.flow(commiterSettings.withMaxBatch(100)))
        .toMat(Sink.seq)(DrainingControl.apply)
        .run()

    scala.io.StdIn
      .readLine(s"consumer started \n press ENTER to stop")
    println("draining")
    val future = drainingControl.drainAndShutdown()
    future.onComplete(_ => system.terminate())
  }
}
