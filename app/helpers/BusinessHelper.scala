package helpers

import scala.util.Random

/**
 * Created by nietaki on 5/15/14.
 */
object BusinessHelper {

  // (26*2 + 10)^10 is slightly less than 2^64, we could fit it in a Long when we transcode it
  val identLength = 10

  def newIdent(): String = Random.alphanumeric.take(identLength).mkString
}
