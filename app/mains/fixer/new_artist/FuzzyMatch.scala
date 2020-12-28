package mains.fixer.new_artist

import scala.annotation.tailrec

private object FuzzyMatch {
  def apply(source: String, dest: String): Boolean = {
    @tailrec def go(source: List[Char], dest: List[Char]): Boolean = (source, dest) match {
      case (Nil, _) => true
      case (_, Nil) => false
      case (sh :: st, dh :: dt) => go(if (sh == dh) st else source, dt)
    }

    go(source.toList, (if (source.exists(_.isUpper)) dest else dest.toLowerCase).toList)
  }
}
