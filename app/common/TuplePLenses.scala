package common

import monocle.PLens

object TuplePLenses {
  // TODO move this to common
  def tuple2Second[A, B, C]: PLens[(A, B), (A, C), B, C] = PLens[(A, B), (A, C), B, C](_._2)(c => _._1 -> c)
}
