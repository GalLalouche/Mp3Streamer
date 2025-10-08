package common.ds

import scala.reflect.ClassTag

import scalaz.IList

object RichIList {
  // TODO Move to ScalaCommon (and then nuke from orbit)
  implicit class richIList[A](private val $ : IList[A]) extends AnyVal {
    def iterator: Iterator[A] = new Iterator[A] {
      private var current: IList[A] = $
      override def hasNext: Boolean = $.nonEmpty
      override def next(): A = {
        val (result, nextCurrent) = current.uncons(???, (_, _))
        current = nextCurrent
        result
      }
    }
    def toArray[B >: A: ClassTag]: Array[B] = iterator.toArray
    def toFuckingMap[K, V](implicit ev: A <:< (K, V)): Map[K, V] = $.iterator.toMap
  }
}
