package helpers

import scala.collection.mutable
import play.api.libs.iteratee.Enumerator

/**
 * Created by nietaki on 5/9/14.
 */
object StateHelper {
  val identEnumeratorsMap = new mutable.LinkedHashMap[String, Enumerator[Array[Byte]]]() with scala.collection.mutable.SynchronizedMap[String, Enumerator[Array[Byte]]]
  val identFilenameMap = new mutable.LinkedHashMap[String, String]() with scala.collection.mutable.SynchronizedMap[String, String]
}
