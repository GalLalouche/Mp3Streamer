package common.concurrency

import backend.FutureOption

import scalaz.OptionT

trait AsyncProducer[Result] extends SimpleTypedActor[Unit, Option[Result]] {
  def !(): FutureOption[Result] = OptionT(this.!(()))
}

object AsyncProducer {
  def apply[Result](name: String, f: => Option[Result]): AsyncProducer[Result] =
    new SimpleTypedActorImpl[Unit, Option[Result]](name, _ => f) with AsyncProducer[Result]
  def async[Result](name: String, f: => FutureOption[Result]): AsyncProducer[Result] =
    new SimpleTypedActorAsyncImpl[Unit, Option[Result]](name, _ => f.run) with AsyncProducer[Result]
}
