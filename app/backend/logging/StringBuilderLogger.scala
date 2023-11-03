package backend.logging

class StringBuilderLogger(sb: StringBuilder) extends StringOutputLogger {
  require(sb != null)
  protected override def output(what: String): Unit = sb.append(what)
}
