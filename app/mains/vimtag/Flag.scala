package mains.vimtag

import java.util.regex.Pattern

import enumeratum.{Enum, EnumEntry}

import scala.collection.immutable

import common.rich.primitives.RichString._
import common.rich.RichT._
import common.rich.primitives.RichBoolean._

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
  object RemoveFeat extends Flag(
    flag = "<REMOVE_FEAT>",
    onByDefault = true,
    comment = "Removes (Feat. X) from title",
  ) {
    private val parens = Pattern.compile(""" +\(Feat.* .*\)""", Pattern.CASE_INSENSITIVE)
    private val noParens = Pattern.compile(""" Feat.* .*""", Pattern.CASE_INSENSITIVE)
    def removeFeat: String => String = _ removeAll parens removeAll noParens
  }
  object RenameFiles extends Flag(
    flag = "<RENAME_FILES>",
    onByDefault = true,
    comment = "Renames the files based on track and title",
  )
  object FixFolder extends Flag(
    flag = "<FIX_FOLDER>",
    onByDefault = true,
    comment = s"Runs FolderFixer after successfully changing the ID3 tags; supersedes ${RenameFiles.flag}",
  )

  def defaultInstructions: Seq[String] = values
      .flatMap(f => Vector("# " + f.comment, f.flag.mapIf(f.onByDefault.isFalse).to("# " + _)))
      .mapIf(_.nonEmpty).to("# Flags look like this <FLAG>. Deleting a flag disables it." +: _)

  def parse(s: String): Option[Flag] = values.find(_.flag == s)
  def parse(xs: Iterable[String]): Set[Flag] = xs.flatMap(parse(_)).toSet

  override def values: immutable.IndexedSeq[Flag] = findValues
}
