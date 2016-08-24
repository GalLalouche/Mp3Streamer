package common.concurrency

import java.util.{Timer, TimerTask}

import akka.actor.Actor

import scala.collection.mutable

/** An actor that ignores repeated tasks */
class LazyActor(sleepTime: Int = 10) extends Actor {
	val timer = new Timer("LazyActor timer")
	var actions = mutable.Set[() => _]()
	override def receive = {
		case f: (() => Any) if actions.contains(f) == false =>
			actions.add(f)
			timer.schedule(new TimerTask() {
				def run() = synchronized {
					f()
					actions.remove(f)
				}
			}, sleepTime)
	}
}
