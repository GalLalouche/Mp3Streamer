package models

import better.files.FileExtensions
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.tag.FieldKey
import org.scalatest.FreeSpec
import org.scalatest.OptionValues.convertOptionToValuable

import scala.concurrent.duration.DurationInt

import common.test.{AuxSpecs, DirectorySpecs}

class IOSongTagParserTest extends FreeSpec with AuxSpecs with DirectorySpecs {
  private def getSong(location: String) = getResourceFile(location)

  "apply" - {
    "parse id3tag" - {
      "mp3" - {
        "common" in {
          val $ = IOSongTagParser(getSong("song.mp3"))
          $.title shouldReturn "Hidden Track"
          $.artistName shouldReturn "Sentenced"
          $.albumName shouldReturn "Crimson"
          $.trackNumber shouldReturn 12
          $.year shouldReturn 2000
          $.bitRate shouldReturn "192"
          $.duration shouldReturn 3.seconds
          $.size shouldReturn 75522L
          $.discNumber shouldReturn None
          $.trackGain shouldReturn None
        }
      }
      "parse year correctly" in {
        IOSongTagParser(getSong("songWithFullDate.mp3")).year shouldReturn 1999
      }
      "parse year from directory if no year tag" in {
        val existingFile = getResourceFile("song.mp3")
        val dir = tempDir.addSubDir("2020 foo bar")
        val copiedFile = existingFile.toScala.copyTo(dir.\(existingFile.getName).toScala).toJava

        val audioFile = AudioFileIO.read(copiedFile)
        val tag = audioFile.getTag
        tag.deleteField(FieldKey.YEAR)
        audioFile.delete()
        audioFile.setTag(tag)
        audioFile.commit()

        IOSongTagParser(copiedFile).year shouldReturn 2020
      }
      "non-empty optionals" in {
        val $ : Song = IOSongTagParser(getSong("songWithMoreInfo.mp3"))
        $.discNumber.value shouldReturn "Foobar"
        $.trackGain.value shouldReturn -1.25
        $.opus.value shouldReturn "Op. 42"
        $.composer.value shouldReturn "Traditional"
        $.conductor.value shouldReturn "yes"
        $.orchestra.value shouldReturn "no"
        $.performanceYear.value shouldReturn 1995
      }
    }

    "flac" - {
      "regular" in {
        val $ = IOSongTagParser(getSong("flacSong.flac"))
        $.title shouldReturn "Hidden Track"
        $.artistName shouldReturn "Ben Folds Five"
        $.albumName shouldReturn "Whatever and Ever Amen"
        $.trackNumber shouldReturn 1 // Track is actually equal to "01/08".
        $.year shouldReturn 1997
        $.discNumber shouldReturn None
        $.trackGain shouldReturn None
      }
      "with optionals" in {
        val $ = IOSongTagParser(getSong("flacWithMoreInfo.flac"))
        $.discNumber.value shouldReturn "1/2"
        $.trackGain.value shouldReturn 1.25
        $.opus.value shouldReturn "BWV 16"
        $.composer.value shouldReturn "Ben Folds"
        $.conductor.value shouldReturn "Condi"
        $.orchestra.value shouldReturn "Orci"
        $.performanceYear.value shouldReturn 1999
      }
    }
  }

  "extractYearFromName" - {
    "No year in name" in {
      IOSongTagParser.extractYearFromName("foobar") shouldReturn None
    }
    "year in name" in {
      IOSongTagParser.extractYearFromName("blah blah 1234 blah") shouldReturn Some(1234)
    }
    "multiple same years succeeds" in {
      IOSongTagParser.extractYearFromName("blah 5678 blah 5678 blah") shouldReturn Some(5678)
    }
    "multiple years returns None" in {
      IOSongTagParser.extractYearFromName("blah 5678 blah 1234 blah") shouldReturn None
    }
  }
}
