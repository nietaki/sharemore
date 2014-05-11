package helpers

import play.api.libs.iteratee._
import play.api.mvc.SimpleResult
import scala.concurrent.Future
import play.api.libs.iteratee.Step

/**
 * Created by nietaki on 5/10/14.
 */
object IterateeHelpers {
/*
  def cancellableIteratee[E, T](innerIteratee: Iteratee[E, Either[SimpleResult, T]], doCancel: Future[Unit.type]): Iteratee[E, Either[SimpleResult, T]] = {

    def augmented(inner: Iteratee[E, Either[SimpleResult, T]]): Iteratee[E, Either[SimpleResult, T]] = inner match {
      case Step.Done(a:A, e) => new Step.Done[E, Either[SimpleResult, T]](a, e)
      case e: Step.Error[E] => println("e"); e
      case cont: Step.Cont[E, Either[SimpleResult, T]] => {
        println("cont")
        val k = cont.k
        def augmentedStep(i: Input[E]): Iteratee[E, Either[SimpleResult, T]] = {
          doCancel.value match {
            case Some(_) => Error[E]("cancelled", i)
            case _ => augmented(cont.k(i))
          }
        }
        Cont(augmentedStep)
      }
      case rest => println(rest); throw new RuntimeException("what?")
    }

    augmented(innerIteratee)
  }
*/
}
