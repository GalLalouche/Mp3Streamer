package common

import org.junit.runner.RunWith
import org.specs2.mutable.Specification
import org.specs2.runner.JUnitRunner

/**
  * Add your spec here.
  * You can mock out a whole application including requests, plugins etc.
  * For more information, consult the wiki.
  */
@RunWith(classOf[JUnitRunner])
class TreeTest extends Specification {

	"Empty Tree" should {
		val $ = Tree()
		"have height zero" in { $.height === 0 }
		"have size zero" in { $.size === 0 }
		"print nothing" in { $.toString === "[]" }
	}
	"Tree with only a root" should {
		val $ = Tree(1)
		"have height 1" in { $.height === 1 }
		"have size 1" in { $.size === 1 }
		"print node" in { $.toString === "[1]" }
	}
	"Simple tree" should {
		val $ = Tree(List(1), Tree(2))
		"print preety" in { $.toString === "[1]\n\t[2]" }
	}
	"Deep Tree" should {
		val $ = Tree(List(1, 2), Tree(sons = List(Tree(1, 2), Tree(3, 4))))
		"have height 2" in { $.height === 3 }
		"have size 6" in { $.size === 6 }
	}
	"printing tests" should {
		"1" in { Tree(List(1, 2), Tree(3, 4)).toString === "[1,2]\n\t[3,4]" }
		"2" in { Tree(List(1, 2), List(Tree(3, 4), Tree(5, 6))).toString === "[1,2]\n\t[3,4]\n\t[5,6]" }
		"3" in {
			Tree(List(1, 2), List(Tree(List(3, 4), Tree(5, 6)))).toString === "[1,2]\n\t[3,4]\n\t\t[5,6]"
		}
		"4" in {
			Tree(List(1, 2), List(Tree(List(3, 4), List(Tree(5, 6),Tree(7,8))), Tree(List(9, 10), Tree(List(11,12), Tree(13,14))))).toString === 
				"[1,2]\n"+
				"\t[3,4]\n"+
				"\t\t[5,6]\n"+
				"\t\t[7,8]\n"+
				"\t[9,10]\n"+
				"\t\t[11,12]\n"+
				"\t\t\t[13,14]"
		}
		"5" in {
			println()
			Tree(List(), List(Tree(List(), Tree(1,2)))).toString === "[]\n\t[]\n\t\t[1,2]"
		}
	}
}