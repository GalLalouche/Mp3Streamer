package backend.logging

class StringBuilderLogger(sb: StringBuilder) extends StringOutputLogger {
  require(sb != null)
  override protected def output(what: String): Unit = println(what)
}

