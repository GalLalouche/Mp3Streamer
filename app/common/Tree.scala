package common

class Tree[+T](val leaves: List[T], val sons: List[Tree[T]]) {
	require(leaves != null)
	require(sons != null)
	protected def printThis = leaves.mkString("[", ",", "]")
	private def mkStringWithPrefix(prefix: String): String = {
		prefix + printThis +
			(if (sons isEmpty) "" else "\n" + sons.map(_.mkStringWithPrefix(prefix + "\t")).mkString("\n"))
	}
	override def toString = mkStringWithPrefix("")

	val height: Int = ((if (leaves isEmpty) 0 else 1) :: sons.map(_.height + 1)) max
	protected def getSize = sons.map(_.size).sum + leaves.length
	lazy val size: Int = getSize
	val isLeaf = height == 0  
}

object Tree {
	def apply[T](leaves: List[_ <: T] = List(), sons: List[_ <: Tree[T]] = List()) = new Tree(leaves, sons)
	def apply[T](leaves: T*) = new Tree(leaves.toList, List())
	def apply[T](leaves: List[_ <: T], sons: Tree[T]*) = new Tree(leaves, sons.toList)
}