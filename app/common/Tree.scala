package common

/**
  * Basically a generic "directory" structure
  */
class Tree[+T](val leaves: Seq[T], val sons: Seq[Tree[T]]) {
	require(leaves != null)
	require(sons != null)
	protected def printThis = leaves.mkString("[", ",", "]")
	private def mkStringWithPrefix(prefix: String): String = {
		prefix + printThis +
			(if (sons isEmpty) "" else "\n" + sons.map(_.mkStringWithPrefix(prefix + "\t")).mkString("\n"))
	}
	override def toString = mkStringWithPrefix("")

	val height: Int = ((if (leaves isEmpty) 0 else 1) :: sons.map(_.height + 1).toList) max
	protected def getSize = sons.map(_.size).sum + leaves.length
	lazy val size: Int = getSize
	val isLeaf = height == 0
}

object Tree {
	def apply[T](leaves: Seq[T], sons: Seq[Tree[T]] = Vector()) = new Tree(leaves, sons)
	private[common] def apply[T](leaves: T*) = new Tree(leaves.toVector, Vector())
	private[common] def apply[T](leaves: List[T], sons: Tree[T]*) = new Tree(leaves, sons.toVector)
}