package helpers

import akka.actor.{ActorRef, Actor}
import scala.collection.mutable.{HashMap, MultiMap, Set}
import BusinessHelper._

/**
 * Created by nietaki on 5/15/14.
 */
class TransferSupervisor extends Actor {

  val listeners: MultiMap[String, ActorRef] = new HashMap[String, Set[ActorRef]] with MultiMap[String, ActorRef]

  def forEachListener(ident: String)(f: ActorRef => Unit) = {
    listeners.get(ident).foreach(set => set.foreach(f))
  }

  def receive = {
    case Subscribe(actorRef, ident) => {
      listeners.addBinding(ident, actorRef)
    }
    case Unsubscribe(actorRef, ident) => {
      listeners.removeBinding(ident, actorRef)
    }
    case ttm: DirectTransferMessage => {
      //forwarding the info to all subscribers
      forEachListener(ttm.ident)(_ ! ttm)
    }
    case sth => println(s"TransferSupervisor received $sth")
  }
}

