package example.http

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
//implicit marshal unmarshal etc
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.server.Directives.{as, complete, entity, path, post}
import akka.http.scaladsl.server.Route
import spray.json.DefaultJsonProtocol.jsonFormat2
import spray.json.RootJsonFormat
//implicit spray.json.DefaultJsonProtocol.JF etc
import spray.json.DefaultJsonProtocol._

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.io.StdIn


object RestProtocol {
  final case class Req(id: String, num: Int)
  final case class Res(id: String, num: Int, title: String = "res")
}

object HttpServerRest {

  def main(args: Array[String]): Unit = {

    implicit val system: ActorSystem[Any] =
      ActorSystem(Behaviors.empty, "simple-api")

    implicit val executionContext: ExecutionContextExecutor =
      system.executionContext

    implicit val reqFormat : RootJsonFormat[RestProtocol.Req] =
      jsonFormat2(RestProtocol.Req)
    implicit val resFormat : RootJsonFormat[RestProtocol.Res] =
      jsonFormat3(RestProtocol.Res)

    val route: Route = path("api") {
      post {
        entity(as[RestProtocol.Req]) {req =>
          val res = RestProtocol.Res(req.id, req.num)
          complete(res)
        }
      }
    }

    val bindingFuture: Future[ServerBinding] =
      Http().newServerAt("localhost", 8080).bind(route)

    println(s"server at localhost:8080 \nPress RETURN to stop")
    StdIn.readLine()
    bindingFuture
      .flatMap(_.unbind())
      .onComplete(_ => system.terminate())
  }

}
