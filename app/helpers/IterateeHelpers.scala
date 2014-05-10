package helpers

import play.api.libs.iteratee._
import play.api.mvc.SimpleResult
import scala.concurrent.Future
import play.api.libs.iteratee.Step

/**
 * Created by nietaki on 5/10/14.
 */
object IterateeHelpers {

  def cancellableIteratee[E, T](innerIteratee: Iteratee[E, Either[SimpleResult, T]], doCancel: Future[Unit]): Iteratee[E, Either[SimpleResult, T]] = {

    def augmented(inner: Iteratee[E, Either[SimpleResult, T]]): Iteratee[E, Either[SimpleResult, T]] = inner match {
      case d: Step.Done[E, Either[SimpleResult, T]] => d
      case e: Step.Error[E] => e
      case cont: Step.Cont[E, Either[SimpleResult, T]] => {
        val k = cont.k
        def augmentedStep(i: Input[E]): Iteratee[E, Either[SimpleResult, T]] = {
          doCancel.value match {
            case Some(_) => Error[E]("cancelled", i)
            case _ => augmented(cont.k(i))
          }
        }
        Cont(augmentedStep)
      }
    }

    augmented(innerIteratee)
  }

}
