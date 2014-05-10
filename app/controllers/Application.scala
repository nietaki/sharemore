package controllers

import helpers.StateHelper
import play.api._
import play.api.mvc._
import play.api.libs.iteratee._
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.concurrent.Promise
import scala.concurrent.Future
import play.api.mvc.BodyParsers.parse.Multipart

object Application extends Controller {

  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }

  val fastBodyAccumulator = BodyParser(
    request => Iteratee.foldM[Array[Byte],Array[Byte]](Array())(
      (state, content) =>
        Future({
          print(s"chunk of ${content.length} bytes\n")
          state ++ content
        })
      ).map(Right(_))
  )

  def filePartHandler(id: Long) = Multipart.handleFilePart({
    case Multipart.FileInfo(partName, filename, contentType) => {
      val (iteratee, enumerator) = Concurrent.joined[Array[Byte]]
      StateHelper.requestIdEnumeratorsMap += ((id, enumerator))
      iteratee
    }
  })

  /**
   * it appears like we have to handle all parts of the file
   */
  val ignorer: PartialFunction[Any, Iteratee[Array[Byte], Unit]] = { case _ => Iteratee.ignore }


  def multipartParser(id: Long) = Multipart.multipartParser(filePartHandler(id).orElse(ignorer))

  def containerMultipartBodyParser = BodyParser("containerMultipart")(requestHeader => {
    print(s"the request id is ${requestHeader.id}\n")
    val ret = multipartParser(requestHeader.id).apply(requestHeader).map(x => Right(Unit))
    Promise.timeout[String]("testing RuntimeExceptions", 5000) onComplete { str =>
      throw new RuntimeException(str.get)
    }
    ret.map[Right[Nothing, Unit.type]](x => {
      println("upload parser iteratee is SO done")
      x
    })

  })
  /*
  val enumeratorSaverBodyParser: BodyParser[Unit] = BodyParser("enumeratorBodyParser")( requestHeader => {
    val (iteratee, enumerator) = Concurrent.joined[Array[Byte]]
    print(s"the request id is ${requestHeader.id}\n")
    StateHelper.requestIdEnumeratorsMap += ((requestHeader.id, enumerator))
    iteratee.map(Right(_))
    }
  )*/

  def upload = Action(fastBodyAccumulator) (rq => {
    val str = new String(rq.body.map(_.toChar))
    val (e, chan) = Concurrent.broadcast[String]
    print("before pushing 1\n")
    chan.push("foo")
    print("before pushing 2\n")
    chan.push("bar")
    print("after pushing 2\n")
    Concurrent
    Ok(rq.body).as("application/octet-stream")
  })

  def upload3 = Action(containerMultipartBodyParser) (rq => {
    val reqId = rq.id
    println("upload action!")
    Ok(views.html.index(s"finished uploading file with request id = ${reqId}"))
  })


  def download(id: Long) = Action(rq => {
    StateHelper.requestIdEnumeratorsMap.get(id) match {
      case None => NotFound("404 - not found")
      case Some(enumerator)  => {
        val reportingEnumerator = enumerator.onDoneEnumerating({println("done enumerating the download")})
        StateHelper.requestIdEnumeratorsMap -= id
        Ok.chunked(reportingEnumerator).as("application/octet-stream")
      }
    }
  })
}