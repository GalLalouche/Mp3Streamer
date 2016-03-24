package common.concurrency

trait ProgressObservable {
  protected def apply(listener: String => Unit)
  private val worker = new SimpleActor[String => Unit] {
    override protected def apply(m: String => Unit): Unit = ProgressObservable.this.apply(m)
  }
  def !(): SimpleObservable[String] = {
    val res = new Publisher[String]()
    worker ! (res publish _)
    res
  }
}
