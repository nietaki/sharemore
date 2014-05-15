import helpers.BusinessHelper
import play.api._
import play.api.libs.concurrent.Akka
import akka.actor.{ActorRef, Props}

/**
 * Created by nietaki on 5/15/14.
 */
object Global extends GlobalSettings {
  private var _transferSupervisor: ActorRef = null

  override def onStart(app: Application) = {
    val supervisorRef = Akka.system(app).actorOf(Props[helpers.TransferSupervisor], "transferSupervisor")
    println(supervisorRef)
    BusinessHelper.transferSupervisorRef = supervisorRef
    //println("onStart")
  }

}
