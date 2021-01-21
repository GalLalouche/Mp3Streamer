package mains.fixer.new_artist

import scala.annotation.tailrec

private object FuzzyMatch {
  def apply(source: String, dest: String): Boolean = {
    @tailrec def go(source: List[Char], dest: List[Char]): Boolean = (source, dest) match {
      case (Nil, _) => true
      case (_, Nil) => false
      case ('\'' :: st, dt) =>
        val (sFirst, sRem) = st.splitAt(st.indexOf(' '))
        val (dFirst, dRem) = dt.splitAt(st.indexOf(' '))
        sFirst == dFirst && go(sRem, dRem)
      case (sh :: st, dh :: dt) => go(if (sh == dh) st else source, dt)
    }

    if (source.isEmpty)
      return true
    if (source.head == '^')
      return apply(source.slice(1, 2), dest.take(1)) && apply(source.drop(2), dest.tail)
    go(source.toList, (if (source.exists(_.isUpper)) dest else dest.toLowerCase).toList)
  }
}
