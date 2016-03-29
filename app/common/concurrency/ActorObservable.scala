package common.concurrency

/** Fucking type erasure */
trait ActorObservable {
  protected type Sink = String => Unit
  protected val devNull: Sink = _ => ()
  protected def apply(sink: Sink)
  private val worker = new SimpleActor[String => Unit] {
    override protected def apply(m: String => Unit): Unit = ActorObservable.this.apply(m)
  }
  def !(sink: Sink) {
    worker ! sink
  }
}
