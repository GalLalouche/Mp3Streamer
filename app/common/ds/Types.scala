package common.ds

import scala.collection.SeqView

// TODO move to ScalaCommon
object Types {
  type ViewSeq[A] = SeqView[A, Seq[_]]
}
