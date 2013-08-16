package common

/**
  * Tree with a value in node, but no leaves
  */
class ValueTree[+T](val value: T, leaves: List[T], override val sons: List[ValueTree[T]]) extends Tree[T](leaves, sons) {
	require(value != null)
	
	override def printThis = "(" + value + ")" + (if (leaves.isEmpty == false) super.printThis else "")
	override def getSize = 1 + super.getSize // +1 for value of this
}

object ValueTree {
	def apply[T](t: T, leaves: List[T] = List(), sons: List[ValueTree[T]] = List()) = new ValueTree(t, leaves, sons)
	def apply[T](t: T, leaves: List[T], sons: ValueTree[T]*) = new ValueTree(t, leaves, sons.toList)
}