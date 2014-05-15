package helpers

import akka.actor.{Props, ActorRef, Actor}
import scala.collection.mutable.{HashMap, MultiMap, Set}
import BusinessHelper._


/**
 * an Actor for passing updates and cancel command to the uploader
 *
 * @param myIdent the ident of the file being uploaded
 */
class TransferTracker(val myIdent: String, val observable: ActorRef, val out: ActorRef) extends Actor {

  override def preStart(): Unit = {
    observable ! Subscribe(self, myIdent)
  }

  def receive = {
    case DownloadDone(ident) => {
      assert(ident == myIdent)
      //forwarding the info to all subscribers
      println(s"TransferTrackersees that download done with ident: $ident")
      observable ! Unsubscribe(self, myIdent)
      context.stop(self)
    }
    case sth => println(s"TransferTracker received $sth")
  }
}

object TransferTracker {
  def props(ident: String, observable: ActorRef, out: ActorRef) = Props(new TransferTracker(ident, observable, out))
}

