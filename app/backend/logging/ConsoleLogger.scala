package backend.logging

class ConsoleLogger extends StringOutputLogger {
  override protected def output(what: String): Unit = println(what)
}
