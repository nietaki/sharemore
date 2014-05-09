package controllers

import helpers.StateHelper
import play.api._
import play.api.mvc._
import play.api.libs.iteratee._
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.concurrent.Promise
import scala.concurrent.Future

object Application extends Controller {

  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }

  val slowBodyParser = BodyParser( request => Iteratee.foldM[Array[Byte],Int](0)((c, content) => Promise.timeout({ println(s"got a chunk of length ${content.length}!"); c+1} ,500) ).map(Right(_)))
  val fastBodyCounter = BodyParser(
    request => Iteratee.foldM[Array[Byte],Int](0)(
      (state, content) =>
        //Promise.timeout({ println(s"got a chunk of length ${content.length}!"); c+1} ,500) ).map(Right(_)
        Future(state+1)
        //Promise.timeout({ println(s"got a chunk of length ${content.length}!"); c+1} ,500) ).map(Right(_)
      ).map(Right(_))
  )

  val fastBodyAccumulator = BodyParser(
    request => Iteratee.foldM[Array[Byte],Array[Byte]](Array())(
      (state, content) =>
        Future({
          print(s"chunk of ${content.length} bytes\n")
          state ++ content
        })
      ).map(Right(_))
  )

  val enumeratorSaverBodyParser: BodyParser[Unit] = BodyParser("enumeratorBodyParser")( requestHeader => {
    val (iteratee, enumerator) = Concurrent.joined[Array[Byte]]
    print(s"the request id is ${requestHeader.id}\n")
    StateHelper.requestIdEnumeratorsMap += ((requestHeader.id, enumerator))
    iteratee.map(Right(_))
    }
  )

  def upload2 = Action(slowBodyParser) (rq => {
    Ok(s"got ${rq.body} chunks")
  })

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

  def upload3 = Action(enumeratorSaverBodyParser) (rq => {
    val reqId = rq.id
    Ok(views.html.index(s"finished uploading file with request id = ${reqId}"))
  })


  def download(id: Long) = Action(rq => {
    StateHelper.requestIdEnumeratorsMap.get(id) match {
      case None => NotFound("404 - not found")
      case Some(enumerator)  => {
        StateHelper.requestIdEnumeratorsMap -= id
        Ok.chunked(enumerator)
      }
    }
  })
}