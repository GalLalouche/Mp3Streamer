package common.concurrency

import akka.actor.{ActorSystem, actorRef2Scala}
import akka.testkit.{TestActorRef, TestProbe}
import org.junit.runner.RunWith
import org.specs2.mutable.Specification
import org.specs2.runner.JUnitRunner
import org.specs2.specification.Scope

/**
  * Add your spec here.
  * You can mock out a whole application including requests, plugins etc.
  * For more information, consult the wiki.
  */
@RunWith(classOf[JUnitRunner])
class LazyActorTest extends Specification {
	implicit val x = ActorSystem("test")
	class Context extends Scope {
		val $ = TestActorRef(new LazyActor(1))
		val probe = TestProbe()
		val f = () => {
			probe.ref ! "Hi"
		}
	}

	"Lazy actor" >> {
		"call f at least once" >> new Context {
			$ ! f
			probe expectMsg ("Hi")
		}
		"handle different calls" >> new Context {
			$ ! f
			$ ! { f() }
			probe expectMsg ("Hi")
			probe expectMsg ("Hi")
		}
		"repeat action after some time has passed" >> new Context {
			$ ! f
			Thread.sleep(10)
			$ ! f
			Thread.sleep(10)
			probe expectMsg ("Hi")
			probe expectMsg ("Hi")
		}
	}
}
