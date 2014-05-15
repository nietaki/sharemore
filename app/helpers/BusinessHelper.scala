package helpers

import scala.util.Random
import akka.actor.ActorRef

/**
 * Created by nietaki on 5/15/14.
 */
object BusinessHelper {

  // (26*2 + 10)^10 is slightly less than 2^64, we could fit it in a Long when we transcode it
  val identLength = 10

  def newIdent(): String = Random.alphanumeric.take(identLength).mkString

  def escape(filename: String): String = {
    filename.replaceAll("\\s", "_")
  }

  //val transferSupervisorName = "transferSupervisor"
  //lazy val transferSupervisorPath = "akka://application/user/" + transferSupervisorName

  var transferSupervisorRef: ActorRef = null

  /*
   * messages sent to the supervisor
   */
  case class DownloadDone(ident: String)

  /*
   * messages sent by the transfer trackers to the supervisor
   */
  case class Subscribe(actorRef: ActorRef, ident: String)
  case class Unsubscribe(actorRef: ActorRef, ident: String)
}
