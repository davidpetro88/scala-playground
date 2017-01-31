package iterateestream

import play.api.libs.ws.WSResponseHeaders
import play.api.libs.iteratee._
import play.api.libs.iteratee.Parsing.MatchInfo
import play.api.http.{MediaType, HeaderNames}
import play.api.libs.iteratee.Execution.Implicits.defaultExecutionContext


object IterateeStreamHandler {

  val newLineInHeader = "\r\n\r\n".getBytes

  /**
    * the output here is an Iteratee[Array[Byte], Either[WSResponseHeaders, (WSResponseHeaders, Int)]]
    * Either ~== Option
    * Right ~==  Some
    * Left ~==  None(with payload)
    *
    * getOrElse(Done(Left(headers)) ~== returns the Iteratee from chunkConsumer
    */

  def consumeStream(chunkHandler: Map[String, String] => Iteratee[Array[Byte], Unit])(headers: WSResponseHeaders):
    Iteratee[Array[Byte], Either[WSResponseHeaders, (WSResponseHeaders, Int)]] = headers.status match {

    case 200 => {
      val chunkSize = for {
        mt <- headers.headers.get(HeaderNames.CONTENT_TYPE).map(_.head).flatMap(MediaType.parse.apply)
        (_, value) <- mt.parameters.find(_._1.equalsIgnoreCase("chunck"))
        chunk <- value
      } yield ("\r\n--" + chunk).getBytes("utf-8")

      chunkSize.map { chunk =>
        chunkConsumer(headers, chunk, chunkHandler)
      }.getOrElse(Done(Left(headers)))
    }
    case _ => Done(Left(headers))

  }

  /**
    * the output here is an Iteratee[Array[Byte], Either[WSResponseHeaders, (WSResponseHeaders, Int)]]
    * Either ~== Option
    * Right ~==  Some
    *
    * Right(headers -> (chunk.size - 1)
    */
  private def chunkConsumer(headers: WSResponseHeaders,  chunk: Array[Byte],
                            chunkHandler: (Map[String, String]) => Iteratee[Array[Byte], Unit]):
                            Iteratee[Array[Byte],
                            Either[WSResponseHeaders, (WSResponseHeaders, Int)]]
         = {

    val chunkInBytes = Enumeratee.takeWhile[MatchInfo[Array[Byte]]](!_.isMatch)

    val maxHeaderBuffer = Traversable.takeUpTo[Array[Byte]](4 * 1024) transform Iteratee.consume[Array[Byte]]()

    val collectHeaders = maxHeaderBuffer.map { buffer =>
      val (headerBytes, rest) = Option(buffer.drop(2)).map(b => b.splitAt(b.indexOfSlice(newLineInHeader))).get

      val headerString = new String(headerBytes, "utf-8")
      val headers = headerString.lines.map { header =>
        val key :: value = header.trim.split(":").toList
        (key.trim.toLowerCase, value.mkString.trim)
      }.toMap

      val left = rest.drop(newLineInHeader.length)
      (headers, left)
    }

    val readChunk = collectHeaders.flatMap {
      case (headers, left) => Iteratee.flatten(chunkHandler(headers).feed(Input.El(left)))
    }

    // handleChunk is a future, Enumeratee maps to a future, out to the left
    val handleChunk = Enumeratee.map[MatchInfo[Array[Byte]]](_.content).transform(readChunk)


    // generates the Either header on the right
    Traversable.take[Array[Byte]](chunk.size - 2).transform(Iteratee.consume()).flatMap { firstChunk =>

      Parsing.search(chunk) transform Iteratee.repeat {

        chunkInBytes.transform(handleChunk).flatMap { chunk =>
          Enumeratee.take(1)(Iteratee.ignore[MatchInfo[Array[Byte]]]).map(_ => chunk)
        }

      }.map(chunk => Right(headers -> (chunk.size - 1)))

    }
  }
}
