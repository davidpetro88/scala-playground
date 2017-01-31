import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.stream.ActorMaterializer

import scala.concurrent.Future
import scala.util.{Failure, Success}

/**
  * Created by ubu on 03.05.16.
  */
object SimpleFutureRunner extends App {

  import scala.concurrent.ExecutionContext.Implicits.global
  implicit val system = ActorSystem("client")
  implicit val materializer = ActorMaterializer()

  val responseFuture: Future[HttpResponse] =
    Http(system).singleRequest(HttpRequest(uri = "http://localhost:8080/"))

  responseFuture onComplete {
    case Success(res) => {
      println("case Success" )
      println(res)
      System.exit(0)
    }
    case Failure(t) => println("An error has occured: " + t.getMessage)
  }

}
