package example.http

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.{ExceptionHandler, Route}
import akka.http.scaladsl.server.Directives.{complete, concat, extractUri, get, handleExceptions, path, post}

import scala.concurrent.Future
import scala.io.StdIn

object HttpServer {

  def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem(Behaviors.empty, "simple-api")
    implicit val executionContext = system.executionContext

    val pingRoute: Route =
      path("ping") {
        concat(get {
          complete("pong")
        }, post {
          println("fake storing op")
          complete("ping stored")
        })
      }

    val pongRoute: Route =
      path("pong") {
        get {
          1 / 0
          complete("ping")
        }
      }

    def exceptionHandler = ExceptionHandler {
      case _: ArithmeticException =>
        extractUri { uri =>
          complete(
            HttpEntity(
              ContentTypes.`application/json`,
              s"sorry something went wrong with $uri")
          ) //there are many other parameters like headers or httpprotocol where you can choose http 1.0 or http 1.1
        }
    }

    val pingPong = concat(pingRoute, pongRoute)
    val route: Route =
      handleExceptions(exceptionHandler)(pingPong)

    val bindingFuture: Future[ServerBinding] =
      Http().newServerAt("localhost", 8080).bind(route)

    println(s"server at localhost:8080 \n press ENTER to stop")
    StdIn.readLine()
    bindingFuture
      .flatMap(_.unbind())
      .onComplete(_ => system.terminate())
  }
}
