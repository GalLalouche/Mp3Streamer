package models

import scala.concurrent.duration.DurationInt

import org.scalatest.FreeSpec

import common.rich.path.RichFile._
import common.test.{AuxSpecs, DirectorySpecs}
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.tag.FieldKey

class SongTagParserTest extends FreeSpec with AuxSpecs with DirectorySpecs {
  private def getSong(location: String) = getResourceFile(location)

  "apply" - {
    "parse id3tag" - {
      "mp3" - {
        "common" in {
          val $ = SongTagParser(getSong("song.mp3"))
          $.title shouldReturn "Hidden Track"
          $.artistName shouldReturn "Sentenced"
          $.albumName shouldReturn "Crimson"
          $.track shouldReturn 12
          $.year shouldReturn 2000
          $.bitRate shouldReturn "192"
          $.duration shouldReturn 3.seconds
          $.size shouldReturn 75522L
          $.discNumber shouldReturn None
          $.trackGain shouldReturn None
        }
      }
      "parse year correctly" in {
        SongTagParser(getSong("songWithFullDate.mp3")).year shouldReturn 1999
      }
      "parse year from directory if no year tag" in {
        val existingFile = getResourceFile("song.mp3")
        val dir = tempDir.addSubDir("2020 foo bar")
        val copiedFile = existingFile.copyTo(dir)

        val audioFile = AudioFileIO.read(copiedFile)
        val tag = audioFile.getTag
        tag.deleteField(FieldKey.YEAR)
        audioFile.delete()
        audioFile.setTag(tag)
        audioFile.commit()

        SongTagParser(copiedFile).year shouldReturn 2020
      }
      "non-empty optionals" in {
        val $ : Song = SongTagParser(getSong("songWithMoreInfo.mp3"))
        $.discNumber.get shouldReturn "Foobar"
        $.trackGain.get shouldReturn -1.25
        $.opus.get shouldReturn "Op. 42"
        $.composer.get shouldReturn "Traditional"
        $.conductor.get shouldReturn "yes"
        $.orchestra.get shouldReturn "no"
        $.performanceYear.get shouldReturn 1995
      }
    }

    "flac" - {
      "regular" in {
        val $ = SongTagParser(getSong("flacSong.flac"))
        $.title shouldReturn "Hidden Track"
        $.artistName shouldReturn "Ben Folds Five"
        $.albumName shouldReturn "Whatever and Ever Amen"
        $.track shouldReturn 1 // Track is actually equal to "01/08".
        $.year shouldReturn 1997
        $.discNumber shouldReturn None
        $.trackGain shouldReturn None
      }
      "with optionals" in {
        val $ = SongTagParser(getSong("flacWithMoreInfo.flac"))
        $.discNumber.get shouldReturn "1/2"
        $.trackGain.get shouldReturn 1.25
        $.opus.get shouldReturn "BWV 16"
        $.composer.get shouldReturn "Ben Folds"
        $.conductor.get shouldReturn "Condi"
        $.orchestra.get shouldReturn "Orci"
        $.performanceYear.get shouldReturn 1999
      }
    }
  }

  "extractYearFromName" - {
    "No year in name" in {
      SongTagParser.extractYearFromName("foobar") shouldReturn None
    }
    "year in name" in {
      SongTagParser.extractYearFromName("blah blah 1234 blah") shouldReturn Some(1234)
    }
    "multiple same years succeeds" in {
      SongTagParser.extractYearFromName("blah 5678 blah 5678 blah") shouldReturn Some(5678)
    }
    "multiple years throws" in {
      an[Exception] shouldBe thrownBy {
        SongTagParser.extractYearFromName("blah 5678 blah 1234 blah")
      }
    }
  }
}
