package musicfinder

import org.scalatest.OneInstancePerTest
import org.scalatest.freespec.AnyFreeSpec

import common.io.MemoryRoot
import common.rich.collections.RichTraversableOnce._
import common.rx.RichObservable.richObservable
import common.test.AuxSpecs

class MusicFilesTest extends AnyFreeSpec with OneInstancePerTest with AuxSpecs {
  private val root = new MemoryRoot
  private val mf = new FakeMusicFilesImpl(
    root,
    genresWithSubGenres = Vector("a", "b", "c"),
    flatGenres = Vector("d"),
  ) {
    (genresWithSubGenres ++ flatGenres).foreach(root.addSubDir)
  }

  "getSongFilters" - {
    "find nothing" - {
      def verifyIsEmpty() = mf.getSongFiles.toVectorBlocking shouldBe empty
      "when subdirs are empty" in {
        root.addSubDir("a").addSubDir("b").addSubDir("c")
        root.addSubDir("b").addSubDir("b").addSubDir("c")
        root.addSubDir("c").addSubDir("b").addSubDir("c")
        verifyIsEmpty()
      }
      "when file is in root" in {
        root.addFile("foo.mp3")
        verifyIsEmpty()
      }
      "when file is in unlisted dir" in {
        root.addSubDir("d").addFile("foo.mp3")
        verifyIsEmpty()
      }
      "when file has wrong extension" in {
        root.addSubDir("b").addFile("foo.mp2")
        verifyIsEmpty()
      }
    }

    "Find song" in {
      val f = root.addSubDir("a").addSubDir("b").addFile("foo.mp3")
      mf.getSongFiles.toVectorBlocking.single shouldReturn f
    }
  }

  "dirs" - {
    val a = root.getDir("a").get
    val b = root.getDir("b").get
    root.getDir("c").get
    val d = root.getDir("d").get
    "Artists in flat and subgenres" in {
      val artistInSubGenre = a.addSubDir("a'").addSubDir("a''")
      b.addSubDir("b'") // Folder is assumed to be a subgenre and therefore should not be listed.
      val artistInFlatGenre = d.addSubDir("d'")

      mf.artistDirs.toVectorBlocking shouldMultiSetEqual Vector(artistInSubGenre, artistInFlatGenre)
    }
    "albums in flat and subgenres" in {
      val artistWithSong = a.addSubDir("a'").addSubDir("a''").addSubDir("a'''")
      a.addSubDir("a!").addSubDir("a''").addSubDir("a'''") // Album without songs
      artistWithSong.addFile("song.mp3")
      val subGenreWithSong = b.addSubDir("b'").addSubDir("b''")
      b.addSubDir("b!").addSubDir("b''") // Artist without songs
      subGenreWithSong.addFile("song.mp3")
      val flatArtistWithSong = d.addSubDir("d'").addSubDir("d''")
      flatArtistWithSong.addFile("song.mp3")
      d.addSubDir("d'").addSubDir("d''") // Flat artist without songs

      mf.albumDirs.toVectorBlocking shouldMultiSetEqual Vector(
        artistWithSong,
        subGenreWithSong,
        flatArtistWithSong,
      )
    }
  }
}
