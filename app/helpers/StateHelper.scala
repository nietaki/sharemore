package helpers

import scala.collection.mutable
import play.api.libs.iteratee.Enumerator

/**
 * Created by nietaki on 5/9/14.
 */
object StateHelper {
  val requestIdEnumeratorsMap = new mutable.LinkedHashMap[Long, Enumerator[Array[Byte]]]() with scala.collection.mutable.SynchronizedMap[Long, Enumerator[Array[Byte]]]

}
