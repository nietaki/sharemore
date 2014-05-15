package helpers

import akka.actor.{Props, ActorRef, Actor}
import scala.collection.mutable.{HashMap, MultiMap, Set}
import BusinessHelper._
import play.api.libs.json._


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


  def receive = {
    case DownloadDone(ident) => {
      assert(ident == myIdent)
      //forwarding the info to all subscribers
      println(s"TransferTrackersees that download done with ident: $ident")
      out ! new JsObject(List(("command", new JsString("abort"))))
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

