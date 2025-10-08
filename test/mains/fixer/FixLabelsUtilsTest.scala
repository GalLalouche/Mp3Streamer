package mains.fixer

import backend.module.TestModuleConfiguration
import models.FakeModelFactory
import net.codingwell.scalaguice.InjectorExtensions.ScalaInjector
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.tag.{FieldKey, Tag}
import org.scalatest.FreeSpec

import scala.jdk.CollectionConverters._

import common.test.AuxSpecs

class FixLabelsUtilsTest extends FreeSpec with AuxSpecs {
  private val $ = new TestModuleConfiguration().injector.instance[FixLabelsUtils]

  private def getSongFile(path: String) = AudioFileIO.read(getResourceFile("../../models/" + path))
  private def getTagValue(t: Tag)(f: FieldKey): String = t.getFirst(f)

  "fixTag" - {
    "mp3" - {
      "basic info" - {
        val fixedTag = $.getFixedTag(getSongFile("song.mp3"), fixDiscNumber = false)
        "correct fixes" in {
          val getTag = getTagValue(fixedTag) _
          getTag(FieldKey.TITLE) shouldReturn "Hidden Track"
          getTag(FieldKey.ARTIST) shouldReturn "Sentenced"
          getTag(FieldKey.ALBUM) shouldReturn "Crimson"
          getTag(FieldKey.TRACK).toInt shouldReturn 12
          getTag(FieldKey.YEAR).toInt shouldReturn 2000
        }
        "No extra attributes" in {
          fixedTag.getFields.asScala.size shouldReturn 5
        }
        "Bonus track suffix is added to disc number" in {
          val tag = $.getFixedTag(getSongFile("songWithBonusTrackName.mp3"), fixDiscNumber = false)
          getTagValue(tag)(FieldKey.DISC_NO) shouldReturn "Bonus"
          getTagValue(tag)(FieldKey.TITLE) shouldReturn "Hidden Track"
        }
        "Bonus track suffix is added to disc number (brackets)" in {
          val tag =
            $.getFixedTag(getSongFile("songWithBonusTrackNameBrackets.mp3"), fixDiscNumber = false)
          getTagValue(tag)(FieldKey.DISC_NO) shouldReturn "Bonus"
          getTagValue(tag)(FieldKey.TITLE) shouldReturn "Hidden Track"
        }
      }
      "When asked to fix discNumber" - {
        "String is unmodified" in {
          val fixedTag = $.getFixedTag(getSongFile("songWithMoreInfo.mp3"), fixDiscNumber = true)
          getTagValue(fixedTag)(FieldKey.DISC_NO) shouldReturn "Foobar"
        }
        "Partial number is truncated" in {
          val fixedTag = $.getFixedTag(getSongFile("flacWithMoreInfo.flac"), fixDiscNumber = true)
          getTagValue(fixedTag)(FieldKey.DISC_NO) shouldReturn "1"
        }
      }
    }
  }

  "validFileName" - {
    "if already valid returns self" in {
      $.validFileName("foo and the bar 123") shouldReturn "foo and the bar 123"
    }
    "invalid characters are removed" in {
      $.validFileName("foo/bar") shouldReturn "foobar"
    }
    "Should not return multiple spaces" in {
      $.validFileName("foo / bar") shouldReturn "foo bar"
    }
  }

  "newFileName" in {
    val song = new FakeModelFactory().song(trackNumber = 2, title = "foo & the bar!?")
    $.newFileName(song, "ape") shouldReturn "02 - foo & the bar!.ape"
  }

  "isBonusTrack" in {
    // Note that since this is an internal function, it expects a lower case string.
    $.bonusTrackParens("foo (bonus)") shouldReturn Some('(')
    $.bonusTrackParens("foo (bonus track)") shouldReturn Some('(')
    $.bonusTrackParens("foo [bonus]") shouldReturn Some('[')
    $.bonusTrackParens("foo [bonus track]") shouldReturn Some('[')
    $.bonusTrackParens("foo (bunos)") shouldReturn None
  }
}
