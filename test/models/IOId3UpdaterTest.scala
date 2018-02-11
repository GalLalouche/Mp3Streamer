package models

import java.io.File

import backend.id3.{IOId3Updater, Id3Metadata}
import common.rich.path.RichFile._
import common.{AuxSpecs, DirectorySpecs}
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.tag.FieldKey
import org.scalatest.FreeSpec

class IOId3UpdaterTest extends FreeSpec with AuxSpecs with DirectorySpecs {
  private def createCopy(path: String): File = getResourceFile(path) copyTo tempDir
  private def createFlacCopy(): File = createCopy("flacSong.flac")
  private def createMp3Copy(): File = createCopy("song.mp3")
  private def update(f: File, metaData: Id3Metadata): IOSong = {
    IOId3Updater.update(Song(f), metaData)
    Song(f)
  }
  "update" - {
    val metaData = Id3Metadata(
      title = "foo",
      artistName = "bar",
      albumName = "bazz",
      track = 4,
      year = 1969,
      discNumber = "quxx")
    def updateAllData(file: => File): Unit = {
      "updates all data" in {
        val updatedSong = update(file, metaData)

        updatedSong.title shouldReturn "foo"
        updatedSong.artistName shouldReturn "bar"
        updatedSong.albumName shouldReturn "bazz"
        updatedSong.track shouldReturn 4
        AudioFileIO.read(updatedSong.file.file).getTag.getFirst(FieldKey.TRACK) shouldReturn "04"
        updatedSong.year shouldReturn 1969
        updatedSong.discNumber shouldReturn Some("quxx")
      }
    }
    def emptyDiscNumber(file: => File): Unit = {
      "empty discNumber" in {
        val withEmptyDiscNumber = metaData.copy(discNumber = "")
        val updatedSong = update(file, withEmptyDiscNumber)

        updatedSong.discNumber shouldReturn None
      }
    }
    def withMoreInfo(file: => File): Unit = {
      val updatedSong = update(file, metaData)
      updatedSong.trackGain should not be 'empty
    }
    "mp3" - {
      updateAllData(createMp3Copy())
      emptyDiscNumber(createMp3Copy())
      withMoreInfo(createCopy("songWithMoreInfo.mp3"))
    }
    "flac" - {
      updateAllData(createFlacCopy())
      emptyDiscNumber(createFlacCopy())
      withMoreInfo(createCopy("flacWithMoreInfo.flac"))
    }
  }
}
