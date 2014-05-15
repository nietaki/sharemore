package controllers

import helpers._
import play.api._
import play.api.mvc._
import play.api.libs.iteratee._
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.concurrent.Promise
import scala.concurrent.Future
import play.api.mvc.BodyParsers.parse.Multipart

object Application extends Controller {

  def index = Action {
    val ident = BusinessHelper.newIdent()
    Ok(views.html.index(ident))
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

  def filePartHandler(ident: String) = Multipart.handleFilePart({
    case Multipart.FileInfo(partName, filename, contentType) => {
      val (iteratee, enumerator) = Concurrent.joined[Array[Byte]]
      StateHelper.identEnumeratorsMap += ((ident, enumerator))
      iteratee
    }
  })

  /**
   * it appears like we have to handle all parts of the file
   */
  val ignorer: PartialFunction[Any, Iteratee[Array[Byte], Unit]] = { case _ => Iteratee.ignore }

  def multipartParser(ident: String) = Multipart.multipartParser(filePartHandler(ident).orElse(ignorer))

  def containerMultipartBodyParser(ident: String) = BodyParser("containerMultipart")(requestHeader => {
    val ret = multipartParser(ident).apply(requestHeader).map(x => Right(Unit))
    val retDone = ret.map[Right[Nothing, Unit.type]](x => {
      println("upload parser iteratee is SO done")
      x
    })
    retDone
  })

  /*
  val enumeratorSaverBodyParser: BodyParser[Unit] = BodyParser("enumeratorBodyParser")( requestHeader => {
    val (iteratee, enumerator) = Concurrent.joined[Array[Byte]]
    print(s"the request id is ${requestHeader.id}\n")
    StateHelper.requestIdEnumeratorsMap += ((requestHeader.id, enumerator))
    iteratee.map(Right(_))
    }
  )

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
  */

  def upload(ident: String) = Action(containerMultipartBodyParser(ident)) (rq => {
    println("upload action!")
    Ok("TODO: some json here")
  })

  def download(ident: String) = Action(rq => {
    StateHelper.identEnumeratorsMap.get(ident) match {
      case None => NotFound("404 - not found")
      case Some(enumerator)  => {
        val reportingEnumerator = enumerator.onDoneEnumerating({println("done enumerating the download")})
        StateHelper.identEnumeratorsMap -= ident
        Ok.chunked(reportingEnumerator).as("application/octet-stream")
      }
    }
  })
}