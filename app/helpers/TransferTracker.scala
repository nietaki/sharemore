package helpers

import akka.actor.{Cancellable, Props, ActorRef, Actor}
import scala.collection.mutable.{HashMap, MultiMap, Set}
import BusinessHelper._
import play.api.libs.json._
import scala.concurrent.duration.FiniteDuration


/**
 * an Actor for passing updates and cancel command to the uploader
 *
 * @param myIdent the ident of the file being uploaded
 */
class TransferTracker(val myIdent: String, val observable: ActorRef, val out: ActorRef) extends Actor {
  println("transferTracker created")
  override def preStart(): Unit = {
    observable ! Subscribe(self, myIdent)
  }

  override def postStop() = {
    observable ! Unsubscribe(self, myIdent)
  }

  var uploadFinished = false
  def receive = {
    case DownloadCancelled(ident) => {
      assert(ident == myIdent)
      if(!uploadFinished) {
        out ! new JsObject(List(("command", new JsString("abort"))))
      } else {
        println("upload finished, not aborting")
      }
      context.stop(self)
    }
    case UploadFinished(ident) => {
      assert(ident == myIdent)
      uploadFinished = true //if this message arrives first
      println(s"TransferTracker sees UploadFinished done with ident: $ident")
      context.stop(self)
    }
    case js: JsValue => {
      println(s"TransferTracker received JSON $js")
    }
    case sth => println(s"TransferTracker received $sth")
  }
}

object TransferTracker {
  def props(ident: String, observable: ActorRef, out: ActorRef) = Props(new TransferTracker(ident, observable, out))
}

