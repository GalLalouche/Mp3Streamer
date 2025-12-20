package common.concurrency

import scala.concurrent.Future

/** The opposite of an actor *ba dum tss*: takes no input but produces output. */
trait Producer[Result] extends SimpleTypedActor[Unit, Result] {
  def !(): Future[Result] = this.!(())
}

object Producer {
  def unique[Result](name: String, f: => Result): Producer[Result] =
    new UniqueSimpleTypedActorImpl[Unit, Result](name, _ => f) with Producer[Result]
}
