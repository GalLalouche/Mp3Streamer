package mains.vimtag

import enumeratum.{Enum, EnumEntry}

import scala.collection.immutable

import common.rich.RichT._

private sealed case class Flag(
    onByDefault: Boolean,
    comment: String,
    flag: String,
) extends EnumEntry

private object Flag extends Enum[Flag] {
  object ResetTrackNumbers extends Flag(
    flag = "<RESET_TRACK>",
    onByDefault = true,
    comment = "Resets track numbers to start from 01",
  )
  object NoUniformDiscNo extends Flag(
    flag = "<NO_UNIFORM_DISC_NO>",
    onByDefault = true,
    comment = "Removes DISC_NO if all its values are identical",
  )

  def defaultInstructions: Seq[String] = values.filter(_.onByDefault)
      .flatMap(f => Vector("# " + f.comment, f.flag))
      .mapIf(_.nonEmpty).to("# Flags look like this <FLAG>. Deleting a flag disables it." +: _)

  def parse(s: String): Option[Flag] = values.find(_.flag == s)
  def parse(xs: Iterable[String]): Set[Flag] = xs.flatMap(parse(_)).toSet

  override def values: immutable.IndexedSeq[Flag] = findValues
}
