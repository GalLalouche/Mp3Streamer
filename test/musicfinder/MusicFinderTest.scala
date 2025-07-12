package musicfinder

import backend.module.FakeMusicFinder
import backend.recon.Artist
import com.google.common.collect.ImmutableBiMap
import musicfinder.MusicFinder.DirectoryName
import org.scalatest.{FreeSpec, OneInstancePerTest}
import org.scalatest.OptionValues._

import common.io.MemoryRoot
import common.rich.collections.RichTraversableOnce._
import common.test.AuxSpecs

class MusicFinderTest extends FreeSpec with OneInstancePerTest with AuxSpecs {
  private val root = new MemoryRoot
  private val mf = new FakeMusicFinder(root) {
    protected override def genresWithSubGenres = Vector("a", "b", "c")
    override def flatGenres = Vector("d")
    (genresWithSubGenres ++ flatGenres).foreach(root.addSubDir)
    override val extensions = Set("mp3", "flac")
    override val invalidDirectoryNames: ImmutableBiMap[DirectoryName, Artist] =
      ImmutableBiMap.of("foo", Artist("bar"))
  }

  "getSongFilters" - {
    "find nothing" - {
      def verifyIsEmpty() = mf.getSongFiles shouldBe 'empty
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
      mf.getSongFiles.single shouldReturn f
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

      mf.artistDirs shouldMultiSetEqual Vector(artistInSubGenre, artistInFlatGenre)
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

      mf.albumDirs shouldMultiSetEqual Vector(artistWithSong, subGenreWithSong, flatArtistWithSong)
    }
  }

  "findArtistDir" - {
    "basic artist dir" in {
      val c = root.addSubDir("a").addSubDir("b").addSubDir("c")
      mf.findArtistDir(Artist("c")).value shouldReturn c
    }
    "Invalid directory name" in {
      val foo = root.addSubDir("a").addSubDir("b").addSubDir("foo")
      mf.findArtistDir(Artist("BaR")).value shouldReturn foo
    }
  }
}
