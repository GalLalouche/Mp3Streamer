package common

import org.junit.runner.RunWith
import org.scalatest.BeforeAndAfter
import org.scalatest.FlatSpec
import org.specs2.mutable.Specification
import org.specs2.runner.JUnitRunner
import akka.actor.ActorDSL
import akka.actor.ActorSystem
import akka.actor.actorRef2Scala
import org.specs2.specification.AfterExample
import org.specs2.specification.Scope

/**
  * Add your spec here.
  * You can mock out a whole application including requests, plugins etc.
  * For more information, consult the wiki.
  */
@RunWith(classOf[JUnitRunner])
class LazyActorTest extends Specification {
	val sleepTime = 3
	// too lazy to hook up mocikto :\
			val $ = ActorDSL.actor(ActorSystem("migration-system"))(new LazyActor(1))
	class Context extends Scope {
		var count = 0
		val f = () => {
			count += 1
		}
	}

	def sleep = Thread.sleep(sleepTime)

	"Lazy actor" >> {

		"call f" >> {

			"at least once" >> new Context {
				$ ! f
				sleep
				println("end")
				count should be >= 1
			}

			"exactly once" >> new Context {
				$ ! f
				$ ! f
				sleep
				count === 1
			}
		}
		"handle different calls" >> new Context {
			$ ! f
			$ ! { f() }
			sleep
			count === 2
		}
		"do the same action after some time has passed" >> new Context {
			$ ! f
			sleep
			$ ! f
			sleep
			count === 2
		}
	}
}