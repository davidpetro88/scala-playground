import akka.actor.{Actor, ActorSystem, Props}
import scala.concurrent.{Promise }


/**
  * Created by ubu on 03.05.16.
  *
  */
object ExecutionContexts extends App {

  implicit val system = ActorSystem("execute_actor_code")

  val actor = system.actorOf(Props[ContextActor], name = "contextactor")

  val p = Promise[String]

  val replyTo = system.actorOf(Props(new Actor {
    def receive = {
      case reply: String =>
        p.success(reply)
        context.stop(self)
    }
  }))

  import scala.concurrent.ExecutionContext.Implicits.global

  p.future.map(response => {
    println("called back from context actor with message: "+ response)
    println("shutting down actor system " )
    system.shutdown()
  })

  actor.tell(msg = "do work for me", sender = replyTo)


}


class ContextActor extends Actor {

  def receive  = {
    case _ => {

      sender ! "I am done with work"

    }
  }
}
