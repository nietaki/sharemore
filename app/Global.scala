import play.api._
import play.api.libs.concurrent.Akka
import akka.actor.Props

/**
 * Created by nietaki on 5/15/14.
 */
object Global extends GlobalSettings {
  override def onStart(app: Application) = {
    val superVisorRef = Akka.system(app).actorOf(Props[helpers.TransferSupervisor], "transferSupervisor")
    println(superVisorRef)
    //println("onStart")
  }
}
