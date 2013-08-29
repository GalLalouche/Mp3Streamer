package common

/**
  * Tree with a value in node, but no leaves
  */
class ValueTree[+T](val value: T, leaves: Seq[T], override val sons: Seq[ValueTree[T]]) extends Tree[T](leaves, sons) {
	require(value != null)
	
	override def printThis = "(" + value + ")" + (if (leaves.isEmpty == false) super.printThis else "")
	override def getSize = 1 + super.getSize // +1 for value of this
}

object ValueTree {
	def apply[T](t: T, leaves: Seq[T] = Vector(), sons: Seq[ValueTree[T]] = Vector()) = new ValueTree(t, leaves, sons)
}