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

    def step(inner: Iteratee[E, Either[SimpleResult, T]], input: Input[E]): Iteratee[E, Either[SimpleResult, T]] = inner match {
      case d: Step.Done[E, Either[SimpleResult, T]] => d
      case e: Step.Error[E] => e
      case cont: Step.Cont[E, Either[SimpleResult, T]] =>  doCancel.value match {
        case None => cont.k(input)
        case Some(_) => {//this could be an exception
          Error("iterating cancelled", input)
        }
      }
    }

    //starting mapper
    innerIteratee match {
      case d: Step.Done[E, Either[SimpleResult, T]] => d
      case e: Step.Error[E] => e
      case cont: Step.Cont[E, Either[SimpleResult, T]] =>
    }

  }

}
