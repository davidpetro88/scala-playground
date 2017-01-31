package iterateestream


import akka.actor.{Actor, ActorSystem, Props}
import play.api.libs.iteratee.{Cont, Done, Enumerator, Input, Iteratee}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}

/**
  * Created by ubu on 07.05.16.
  */
object SummaryOfFlow {

  implicit val system = ActorSystem("local")

  val actor = system.actorOf(Props[StreamActor], name = "streamactor")


  def main(args: Array[String]) {
    println("call actor")

    actor ! "do work for me"

    println("main thread ends")

  }

}

  class StreamActor extends Actor {

    val enumText: Enumerator[String] = Enumerator("1 2 3 4", "5 6 78", "9 10to", "14")

    val countLetters: Iteratee[String, Int] = {

      def step(acc: Int)(i: Input[String]): Iteratee[String, Int] = i match {

        case Input.EOF => Done(acc, Input.EOF)
        case Input.Empty => Cont[String, Int](i => step(acc)(i))
        // this is a recursive call, stores result in accumalator
        case Input.El(e) => Cont[String, Int](line => step(e.trim.split(" ").length + 1 + acc)(line))

      }

      // first step
      Cont[String, Int](i => step(0)(i))
    }

    def receive  = {
      case _ => {
        val futureEnumText: Future[Int] = enumText |>>> countLetters

        futureEnumText.onComplete {
          case Success(x) =>  {
            println(x)

          }
          case Failure(error) => println(error)
        }

      }
    }
  }
