package common

/** An actor with no input */
trait Extra {
  private val actor = new SimpleActor[Unit] {
    override protected def act(u: Unit) = act(u)
  }
  protected def act() 
  def !() = actor.!(())
}