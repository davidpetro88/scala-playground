package iterateestream

import java.io.File
import java.nio.file.Files
import java.util.concurrent.atomic.AtomicInteger

import akka.util.Timeout
import iterateestream.IterateeStreamHandler.consumeStream
import org.scalatest.EitherValues
import org.scalatestplus.play.{OneServerPerSuite, PlaySpec}
import play.api.libs.iteratee.Execution.Implicits.defaultExecutionContext
import play.api.libs.iteratee.{Cont, Done, Enumerator, Input, Iteratee}
import play.api.libs.ws.WS
import play.api.mvc.Action
import play.api.mvc.Results.Ok
import play.api.test.FakeApplication
import play.api.test.Helpers.await
import play.api.libs.iteratee.Execution.Implicits.defaultExecutionContext

class IterateeStreamHandlerTest extends PlaySpec with OneServerPerSuite with EitherValues {

  implicit override lazy val app: FakeApplication =
    FakeApplication(
      withRoutes = {
        case ("GET", "/upload") => Action {
          // everything starts here
          Ok.chunked(Enumerator.fromFile(mockFile, 1024)).as("multipart/package; chunck=\"partBounds\"")
        }
      }
    )

  "iteratee stream" should {
    "handle  multipart file chunks" in {
      runIterateeStream( 400)
    }
  }

  /**
    *
    * this is the test code
    */
  private def runIterateeStream(checkSum: Int) {
    val chunks = new AtomicInteger(0)

    /**
      *
      * the output here is an  Iteratee[Array[Byte], Unit]
      * passed into iterateestream.IterateeStreamHandler consumeStream
      *
      * this is the iteration for the stream (Cont)
      * res uses buffer for its byte array
      *
      */
    def chunkConsumer(headers: Map[String, String]): Iteratee[Array[Byte], Unit] = {
      headers.get("link").map { link =>

        var buffer = Array[Byte]()
        lazy val res: Iteratee[Array[Byte], Unit] = Cont {
          case e@Input.EOF =>
            chunks.incrementAndGet()
            if (chunks.intValue() % 100 == 0) println(s"iterated this many chunks ${chunks.intValue()} ")
            Done((), e)
          case in@Input.El(data) =>
            buffer ++= data
            res
          case Input.Empty => res
        }
        res
      }.getOrElse(Done((), Input.Empty))

    }

    /**
      *  In terms of streams Everything starts here
      * this is the stream result as a future
      * we pass the input stream chunkConsumer()
      */
    val futureResponse = WS.url(s"http://localhost:$port/upload")
      .get(consumeStream(chunkConsumer))
      .flatMap(_.run)

    import scala.concurrent.duration._
    implicit val defaultAwaitTimeout: Timeout = 200.seconds
    val response = await(futureResponse)

    response.right.value._1.status mustBe 200
    response.right.value._2 mustBe checkSum

  }


  private def makeMockFile( ) : File = {
    val part = new String((Array.fill(400)("a".getBytes).flatten))
    val multipart = (List.fill(400)(header + "\n\n" + part).mkString("\n") + "\n--partBounds--").replaceAll("\n", "\r\n")
    val f = File.createTempFile(s"multipart-400-400", "tmp")
    Files.write(f.toPath, multipart.getBytes)
    f
  }

  private val header =
    """--partBounds
      |Content-Disposition: inline
      |Content-Transfer-Encoding: 8bit
      |Content-Type: text/plain
      |Link: </dummy/link>
    """.stripMargin.trim

  private val mockFile = makeMockFile( )

}
