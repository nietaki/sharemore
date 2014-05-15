package controllers

import helpers._
import play.api._
import play.api.Play.current
import play.api.libs.json.JsValue

//damn implicit app
import play.api.mvc._
import play.api.libs.iteratee._
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.concurrent.{Akka, Promise}
import scala.concurrent.Future
import play.api.mvc.BodyParsers.parse.Multipart

object Application extends Controller {

  def index = Action { rh =>
    val ident = BusinessHelper.newIdent()

    //val ref = Akka.system.actorSelection(BusinessHelper.transferSupervisorPath)
    //ref ! "test"
    BusinessHelper.transferSupervisorRef ! "test"
    val websocketUrl = routes.Application.status(ident).webSocketURL()(rh)
    Ok(views.html.index(ident, websocketUrl))
  }

  /*
  val fastBodyAccumulator = BodyParser(
    request => Iteratee.foldM[Array[Byte],Array[Byte]](Array())(
      (state, content) =>
        Future({
          print(s"chunk of ${content.length} bytes\n")
          state ++ content
        })
      ).map(Right(_))
  )
  */

  def filePartHandler(ident: String) = Multipart.handleFilePart({
    case Multipart.FileInfo(partName, filename, contentType) => {
      val (iteratee, enumerator) = Concurrent.joined[Array[Byte]]
      StateHelper.identEnumeratorsMap += ((ident, enumerator))
      StateHelper.identFilenameMap += ((ident, filename))
      println(s"filename=$filename")
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
      //val ref = Akka.system.actorSelection(BusinessHelper.transferSupervisorPath)
      BusinessHelper.transferSupervisorRef ! BusinessHelper.DownloadDone(ident)
      x
    })
    retDone
  })

  def upload(ident: String) = Action(containerMultipartBodyParser(ident)) (rq => {
    println("upload action!")
    Ok("TODO: some json here")
  })

  def download(ident: String) = Action {rq =>
    //val filename="tmp.tmp"
    val filenameOption = StateHelper.identFilenameMap.get(ident).map(BusinessHelper.escape(_))
    filenameOption match {
      case None => NotFound("404: upload for this id hasn't been started")
      case Some(filename) => Ok(views.html.download(ident, filename))
    }
  }

  def file(ident: String, filename: String) = Action(rq => {
    StateHelper.identEnumeratorsMap.get(ident) match {
      case None => NotFound("404 - not found")
      case Some(enumerator)  => {
        val storedFilename = BusinessHelper.escape(StateHelper.identFilenameMap(ident))
        if(filename != storedFilename) {
          NotFound("404 - filename incorrect")
        } else {
          val reportingEnumerator = enumerator.onDoneEnumerating({println("done enumerating the download")})
          StateHelper.identEnumeratorsMap -= ident
          StateHelper.identFilenameMap -= ident
          Ok.chunked(reportingEnumerator).as("application/octet-stream")
        }
      }
    }
  })

  //def status(ident: String) = Action(rq => NotFound("tmp"))

  def status(ident: String) = WebSocket.acceptWithActor[JsValue, JsValue](rh =>
    out => TransferTracker.props(ident, BusinessHelper.transferSupervisorRef, out))
}