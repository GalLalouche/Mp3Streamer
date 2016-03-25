package common.concurrency

/** Fucking type erasure */
trait ProgressObservable {
  protected type Sink = String => Unit
  protected val devNull: Sink = _ => ()
  protected def apply(sink: Sink)
  private val worker = new SimpleActor[String => Unit] {
    override protected def apply(m: String => Unit): Unit = ProgressObservable.this.apply(m)
  }
  def !(sink: Sink) {
    worker ! sink
  }
}
