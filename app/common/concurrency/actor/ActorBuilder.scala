package common.concurrency.actor

import scala.concurrent.Future

sealed trait ActorBuilder[Msg, Result] {
  def apply(f: Msg => Result): Actor[Msg, Result]
  def async(f: Msg => Future[Result]): Actor[Msg, Result]
  /** The opposite of an actor *ba dum tss*: takes no input but produces output. */
  def producer(dirs: => Result): Actor[Any, Result]
  def self(f: (Actor[Msg, Result], Msg) => Result): Actor[Msg, Result]
}

private object ActorBuilder {
  trait ActorConstructor {
    def apply[Msg, Result](f: ActorFunction[Msg, Result]): Actor[Msg, Result]
  }
  def builder[Msg, Result](
      ctor: ActorConstructor,
  ): ActorBuilder[Msg, Result] = new ActorBuilder[Msg, Result] {
    override def apply(f: Msg => Result): Actor[Msg, Result] =
      selfAsync[Msg, Result](ctor, (_, msg) => Future.successful(f(msg)))
    override def async(f: Msg => Future[Result]): Actor[Msg, Result] =
      selfAsync[Msg, Result](ctor, (_, msg) => f(msg))
    override def self(f: (Actor[Msg, Result], Msg) => Result): Actor[Msg, Result] =
      selfAsync[Msg, Result](ctor, (self, msg) => Future.successful(f(self, msg)))
    override def producer(result: => Result): Actor[Any, Result] =
      selfAsync[Any, Result](ctor, (_, _) => Future.successful(result))
  }

  /**
   * This exists for a common reduction point for all of the above but should not be used directly
   * to avoid deadlocks.
   */
  private def selfAsync[Msg, Result](
      ctor: ActorConstructor,
      f: (Actor[Msg, Result], Msg) => Future[Result],
  ): Actor[Msg, Result] = ctor((self: Actor[Msg, Result], msg: Msg) => f(self, msg))
}
