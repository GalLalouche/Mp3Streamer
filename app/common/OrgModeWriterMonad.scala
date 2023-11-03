package common

import scalaz.State

// This is a cool little exercise! (Assuming it works). Maybe for the tutorials
object OrgModeWriterMonad {
  type OrgModeWriterMonad = State[OrgModeWriter, Unit]
  def run(m: OrgModeWriterMonad): OrgModeWriter = m.exec(OrgModeWriter())
  def indent(m: OrgModeWriterMonad): OrgModeWriterMonad = State.modify(_.indent(m.exec))
  def append(line: String): OrgModeWriterMonad = State.modify(_.append(line))
  def appendAll(lines: Seq[String]): OrgModeWriterMonad = State.modify(_.appendAll(lines))
}
