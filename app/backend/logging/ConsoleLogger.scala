package backend.logging

// A trait so it can be mixed with FilteringLogger
trait ConsoleLogger extends StringOutputLogger {
  protected override def output(what: String): Unit = println(what)
}
object ConsoleLogger extends ConsoleLogger
