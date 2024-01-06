package mains.vimtag

import backend.module.FakeMusicFinder
import models.FakeModelFactory
import models.Song.TrackNumber
import org.scalatest.{FreeSpec, OneInstancePerTest}

import common.io.MemoryRoot
import common.test.AuxSpecs

abstract class InitializerParserTest(ii: IndividualInitializer, ip: IndividualParser)
    extends FreeSpec
    with AuxSpecs
    with OneInstancePerTest {
  private val mf = new FakeMusicFinder(new MemoryRoot)
  private val factory = new FakeModelFactory
  "A non-interactive initializer-parser couple returns the correct ID3" in {
    def newSong(
        track: TrackNumber,
        title: String,
        year: Int,
        discNumber: Option[String],
        conductor: Option[String],
    ) =
      mf.copySong(
        "dir",
        factory.song(
          filePath = s"$track - $title",
          year = year,
          artistName = "Evgeny Kissin",
          albumName = "Prokofiev - Piano Concerto No. 2 & 3 (Kissin, Ashkenazy)",
          composer = Some("Prokofiev"),
          orchestra = Some("Philharmonia Orchestra"),
          performanceYear = Some(2009),
          track = track,
          title = title,
          discNumber = discNumber,
          conductor = conductor,
        ),
      )

    val s1 = newSong(
      5,
      "Piano Concerto No.3 in C Major, Op.26 - I. Andante - Allegro",
      2001,
      None,
      Some("Vladimir Ashkenazy"),
    )
    val s2 = newSong(
      6,
      "Piano Concerto No.3 in C Major, Op.26 - II. Tema & Variation",
      2002,
      Some("Foobar"),
      None,
    )
    val s3 = newSong(
      9,
      "Piano Concerto No.3 in C Major, Op.26 - III. Allegro, ma non troppo",
      2003,
      None,
      None,
    )

    val parser = new Parser(ip)
    val initializer = new Initializer(mf, ii)

    val initial = initializer.apply(s1.file.parent)
    val res = parser(initial.initialValues)(initial.lines)

    res.artist shouldReturn Change("Evgeny Kissin")
    res.album shouldReturn Change("Prokofiev - Piano Concerto No. 2 & 3 (Kissin, Ashkenazy)")
    res.year shouldReturn Keep

    res.composer shouldReturn Change("Prokofiev")
    res.opus shouldReturn Empty
    res.conductor shouldReturn Change("Vladimir Ashkenazy")
    res.orchestra shouldReturn Change("Philharmonia Orchestra")
    res.performanceYear shouldReturn Change(2009)

    val songs = res.songId3s
    songs should have size 3
    val song1 = songs(0)
    song1.relativeFileName shouldReturn s1.file.name
    song1.title shouldReturn "Piano Concerto No.3 in C Major, Op.26 - I. Andante - Allegro"
    song1.track shouldReturn 5
    song1.discNumber shouldReturn None

    val song2 = songs(1)
    song2.relativeFileName shouldReturn s2.file.name
    song2.title shouldReturn "Piano Concerto No.3 in C Major, Op.26 - II. Tema & Variation"
    song2.track shouldReturn 6
    song2.discNumber.get shouldReturn "Foobar"

    val song3 = songs(2)
    song3.relativeFileName shouldReturn s3.file.name
    song3.title shouldReturn "Piano Concerto No.3 in C Major, Op.26 - III. Allegro, ma non troppo"
    song3.track shouldReturn 9
    song3.discNumber shouldReturn None
  }

  "Ordering" in {
    def newSong(track: TrackNumber, fileName: String) =
      mf.copySong(
        "dir",
        factory.song(
          filePath = fileName,
          year = 2000,
          artistName = "Some artist",
          albumName = "Some album",
          track = track,
          title = "whatever",
        ),
      )

    newSong(2, "c")
    newSong(1, "b")
    val dir = newSong(3, "a").file.parent

    val parser = new Parser(ip)
    val initializer = new Initializer(mf, ii)

    val initial = initializer.apply(dir)
    val res = parser(initial.initialValues)(initial.lines)
    val songs = res.songId3s
    songs(0).track shouldReturn 1
    songs(1).track shouldReturn 2
    songs(2).track shouldReturn 3
  }

  "Multiple folders" in {
    def newSong(track: TrackNumber, subName: String, fileName: String) =
      mf.copySong(
        Vector("dir", subName),
        factory.song(
          filePath = fileName,
          year = 2000,
          artistName = "Some artist",
          albumName = "Some album",
          track = track,
          title = "whatever",
        ),
      )

    newSong(1, "a", "a")
    newSong(2, "a", "b")
    newSong(1, "b", "c")
    val dir = newSong(2, "b", "d").file.parent.parent

    val parser = new Parser(ip)
    val initializer = new Initializer(mf, ii)

    val initial = initializer.apply(dir)
    val res = parser(initial.initialValues)(initial.lines)
    val songs = res.songId3s
    songs(0).track shouldReturn 1
    songs(0).relativeFileName shouldReturn "a/a"
    songs(1).track shouldReturn 2
    songs(1).relativeFileName shouldReturn "a/b"
    songs(2).track shouldReturn 1
    songs(2).relativeFileName shouldReturn "b/c"
    songs(3).track shouldReturn 2
    songs(3).relativeFileName shouldReturn "b/d"
  }
}
