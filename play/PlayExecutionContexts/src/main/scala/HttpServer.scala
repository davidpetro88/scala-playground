import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.HttpMethods._
import akka.stream.ActorMaterializer


/**
 * Created by ubu on 02.05.16.  class DadsServer extends Runnable
 */
object HttpServer extends App   {



    implicit val system = ActorSystem("my-system")
    implicit val materializer = ActorMaterializer()
    import system.dispatcher

    val requestHandler: HttpRequest => HttpResponse = {
      case HttpRequest(GET, Uri.Path("/"), _, _, _) =>
        HttpResponse(entity = HttpEntity(MediaTypes.`text/html`,
          "<html><body>Hello world!</body></html>"))

      case _: HttpRequest =>
        HttpResponse(404, entity = "Unknown resource!")
    }


    val bindingFuture = Http().bindAndHandleSync(requestHandler, "localhost", 8080)

    println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
    Console.readLine()

    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ ⇒ system.shutdown()) // and shutdown when done

}
