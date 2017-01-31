
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{Uri, HttpRequest}
import akka.stream.ActorMaterializer

/**
  * Created by ubu on 03.05.16.
  */
object StreamingActors extends App  {

  new Thread(new StreamingServer).start()

  implicit val system = ActorSystem("streaming_client")
  import system.dispatcher

  implicit val materializer = ActorMaterializer()

  val source = Uri("http://localhost:8200/stream")

  val stream = Http().singleRequest(HttpRequest(uri = source)).flatMap { response =>
    response.entity.dataBytes.runForeach { chunk =>
      println(chunk.utf8String)
    }
  }


}
