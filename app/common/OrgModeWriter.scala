package common

/** DSL for writing org files. */
class OrgModeWriter private (currentIndent: Int, private val reversedLines: List[String]) {
  def lines: Seq[String] = reversedLines.reverse.toVector
  def indent(f: OrgModeWriter => OrgModeWriter): OrgModeWriter = {
    val nested = f(new OrgModeWriter(currentIndent + 1, Nil))
    new OrgModeWriter(currentIndent, nested.reversedLines ::: reversedLines)
  }
  def append(line: String): OrgModeWriter =
    new OrgModeWriter(currentIndent, s"${"*" * currentIndent} $line" :: reversedLines)
  def appendAll(lines: Seq[String]): OrgModeWriter = lines.foldLeft(this)(_ append _)
}

object OrgModeWriter {
  def apply() = new OrgModeWriter(1, Nil)
}
