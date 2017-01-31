import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
import scala.concurrent.duration._

class StreamingServer extends Runnable {

	def run() = {

		implicit val system = ActorSystem("streaming_server")
		implicit val materializer = ActorMaterializer()

		val serverSource = Http().bind(interface = "localhost", port = 8200)

		val requestHandler: HttpRequest => HttpResponse = {
			case HttpRequest(GET, Uri.Path("/stream"), _, _, _) =>
				HttpResponse(entity = HttpEntity.Chunked(ContentTypes.`text/plain`,
																					Source(0 seconds, 1 seconds, "test")))
		}

		serverSource.to(Sink.foreach { connection =>
			connection handleWithSyncHandler requestHandler
		}).run()

	}

}