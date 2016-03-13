package mains.cover

//TODO move this to the concurrency package or use one of the actors
/**
 * Hide the mutability as much as possible.
 * HIDE THE SHAME!
 */
private class DelayedThread(name: String) extends AutoCloseable {
	private var thread: Thread = null
	private var started = false
 
	def start(action: () => Unit) {
		if (started)
			throw new IllegalStateException("Can only be started once")
		started = true
		thread = new Thread("Worker: " + name) {
			override def run() {
				try {
					while (!isInterrupted)
						action()
				} catch {
					case e: InterruptedException => ()
				}
			}
		}
		thread.setDaemon(true)
		thread.start()
	}
	
	override def close() {
		thread.interrupt()
	}
}
