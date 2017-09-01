package mains.fixer

import common.AuxSpecs
import org.jaudiotagger.tag.{FieldKey, Tag}
import org.scalatest.FreeSpec

import scala.collection.JavaConversions._

class FixLabelsTest extends FreeSpec with AuxSpecs {
  private def getSongFile(path: String)  = getResourceFile("../../models/" + path)
  private def getTagValue(t: Tag)(f: FieldKey): String = t getFirst f
  "fixTag" - {
    "mp3" - {
      val song = getSongFile("songWithMoreInfo.mp3")
      "basic info" - {
        val fixedTag = FixLabels.fixTag(song, fixDiscNumber = false)
        "correct fixes" in {
          val getTag = getTagValue(fixedTag) _
          getTag(FieldKey.TITLE) shouldReturn "Hidden Track"
          getTag(FieldKey.ARTIST) shouldReturn "Sentenced"
          getTag(FieldKey.ALBUM) shouldReturn "Crimson"
          getTag(FieldKey.TRACK).toInt shouldReturn 12
          getTag(FieldKey.YEAR).toInt shouldReturn 2000
        }
        "No extra attributes" in {
          fixedTag.getFields.size shouldReturn 5
        }
      }
      "When asked to fix discNumber" - {
        "String number" in {
          val fixedTag = FixLabels.fixTag(song, fixDiscNumber = true)
          getTagValue(fixedTag)(FieldKey.DISC_NO) shouldReturn "Foobar"
        }
        "Partial number" in {
          val fixedTag = FixLabels.fixTag(getSongFile("flacWithMoreInfo.flac"), fixDiscNumber = true)
          getTagValue(fixedTag)(FieldKey.DISC_NO) shouldReturn "1"
        }
      }
    }
  }
}
