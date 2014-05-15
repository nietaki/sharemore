package helpers

import akka.actor.{ActorRef, Actor}
import scala.collection.mutable.{HashMap, MultiMap, Set}
import BusinessHelper._


/**
 * an Actor for passing updates and cancel command to the uploader
 *
 * @param myIdent the ident of the file being uploaded
 */
class TransferTracker(val myIdent: String) extends Actor {

   def receive = {
     case DownloadDone(ident) => {
       assert(ident == myIdent)
       //forwarding the info to all subscribers
       println(s"TransferTrackersees that download done with ident: $ident")
     }
     case sth => println(s"TransferTracker received $sth")
   }
 }

