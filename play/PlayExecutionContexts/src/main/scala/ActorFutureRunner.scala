import akka.actor.Actor
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.stream.ActorMaterializer

import scala.concurrent.Future
import scala.util.{Failure, Success}

object Client extends App {

	import akka.actor.{ActorSystem, Props}

	implicit val system = ActorSystem("eddys_actor_code")

	val actor = system.actorOf(Props[EddysActor], name = "eddysactor")

	actor ! "do your stuff"


}

class EddysActor extends Actor {

	import scala.concurrent.ExecutionContext.Implicits.global
	implicit val materializer = ActorMaterializer()

	def receive  = {
		case _ => {
			// use context.system explicitly
			val responseFuture: Future[HttpResponse] = Http(context.system)
				.singleRequest(HttpRequest(uri = "http://localhost:8080/"))

			   responseFuture onComplete {
				case Success(res) => {
					 println("case Success" )
					 println(res)
				}
				case Failure(t) => println("An error has occured: " + t.getMessage)
			}
		}
	}
}