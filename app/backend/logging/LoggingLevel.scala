package backend.logging

import common.rich.RichT._

sealed abstract class LoggingLevel(private val level: Int) extends Ordered[LoggingLevel] {
  // Has to be lazy to avoid infinite constructor recursion.
  lazy val andLower: Traversable[LoggingLevel] = LoggingLevel.values.filter(_.level <= level)

  override def compare(that: LoggingLevel): Int = this.level.compare(that.level)
  override def toString: String = this.simpleName
}

object LoggingLevel {
  object Verbose extends LoggingLevel(0)
  object Debug extends LoggingLevel(1)
  object Info extends LoggingLevel(2)
  object Warn extends LoggingLevel(3)
  object Error extends LoggingLevel(4)
  def values: Traversable[LoggingLevel] = Vector(Verbose, Debug, Info, Warn, Error)
}
