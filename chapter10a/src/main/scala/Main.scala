import akka.{Done, NotUsed}
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.stream.scaladsl.{Flow, Keep, RunnableGraph, Sink, Source}

import scala.concurrent.Future

object Main extends App {

  implicit val system : ActorSystem[Any]  = ActorSystem(Behaviors.empty, "runner")

  var fakeDB: List[Int] = List()
  def storeDB(value: Int): Unit =
    fakeDB = fakeDB :+ value

  val producer: Source[Int, NotUsed] = Source(List(1, 2, 3))
  val processor: Flow[Int, Int, NotUsed] =
    Flow[Int].filter(_ % 2 == 0)
  val consumer: Sink[Int, Future[Done]] =
    Sink.foreach(i => storeDB(i))

  val blueprint: RunnableGraph[Future[Done]] =
    producer.via(processor).toMat(consumer)(Keep.right)

  val future: Future[Done] = blueprint.run()
  future.onComplete(_ => {println(s"db $fakeDB"); system.terminate()})(system.executionContext)

  println(s"db $fakeDB")
  //actor system is asynchronous
}
