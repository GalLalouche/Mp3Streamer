package common

import java.util.Timer
import java.util.TimerTask

import scala.collection.mutable.Set

import akka.actor.Actor

class LazyActor(sleepTime: Int = 10) extends Actor {
	val timer = new Timer("LazyActor timer")
	var actions = Set[Function0[_]]()
	override def receive = {
		case f: Function0[Any] if (actions.contains(f) == false) => {
			actions.add(f)
			timer.schedule(new TimerTask() {
				def run = synchronized {
					f()
					actions.remove(f)
				}
			}, sleepTime)
		}
	}
}