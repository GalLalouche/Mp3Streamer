package mains.vimtag

import java.util.regex.Pattern

import enumeratum.{Enum, EnumEntry}

import scala.collection.immutable

import common.rich.RichT._
import common.rich.collections.RichTraversableOnce.richTraversableOnce
import common.rich.primitives.RichBoolean._
import common.rich.primitives.RichString._

private sealed case class Flag(
    onByDefault: Boolean,
    comment: String,
    flag: String,
    mnemonic: String,
) extends EnumEntry

private object Flag extends Enum[Flag] {
  object Asciify
      extends Flag(
        flag = "<STRICT_ASCII>",
        onByDefault = true,
        comment = "Fail if ASCIIfication failed, and language isn't exempt",
        mnemonic = "sa",
      )
  object ResetTrackNumbers
      extends Flag(
        flag = "<RESET_TRACK>",
        onByDefault = true,
        comment = "Resets track numbers to start from 01",
        mnemonic = "rt",
      )
  object NoUniformDiscNo
      extends Flag(
        flag = "<NO_UNIFORM_DISC_NO>",
        onByDefault = true,
        comment = "Removes DISC_NO if all its values are identical",
        mnemonic = "ud",
      )
  object RemoveFeat
      extends Flag(
        flag = "<REMOVE_FEAT>",
        onByDefault = true,
        comment = "Removes (Feat. X) from title",
        mnemonic = "feat",
      ) {
    private val parens = Pattern.compile(""" +\(F(ea)?t.* .*\)""", Pattern.CASE_INSENSITIVE)
    private val noParens = Pattern.compile(""" F(ea)?t.* .*""", Pattern.CASE_INSENSITIVE)
    def removeFeat: String => String = _.removeAll(parens).removeAll(noParens)
  }
  object RenameFiles
      extends Flag(
        flag = "<RENAME_FILES>",
        onByDefault = true,
        comment = "Renames the files based on track and title",
        mnemonic = "rf",
      )
  object FixFolder
      extends Flag(
        flag = "<FIX_FOLDER>",
        onByDefault = true,
        comment =
          s"Runs FolderFixer after successfully changing the ID3 tags; supersedes ${RenameFiles.flag}",
        mnemonic = "ff",
      )

  def defaultInstructions: Seq[String] = {
    val flagInstructions =
      "# Flags look like this <FLAG> (mnemonic). Deleting or commenting out a flag disables it.\n" +
        "# Alternatively, use <LEADER><mnemonic> to toggle the flag on and off."
    values
      .flatMap(f =>
        Vector(s"# ${f.comment} (${f.mnemonic})", f.flag.mapIf(f.onByDefault.isFalse).to("# " + _)),
      )
      .mapIf(_.nonEmpty)
      .to(flagInstructions +: _)
  }

  def parse(s: String): Option[Flag] = values.find(_.flag == s)
  def parse(xs: Iterable[String]): Set[Flag] = xs.flatMap(parse(_)).toSet

  override val values: immutable.IndexedSeq[Flag] = {
    val result = findValues
    assert(
      result.map(_.mnemonic).allUnique,
      "Found repeat mnemonics in: " + result.map(_.toTuple(_.flag, _.mnemonic)),
    )
    result
  }
}
