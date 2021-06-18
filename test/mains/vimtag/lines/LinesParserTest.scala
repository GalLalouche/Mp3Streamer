package mains.vimtag.lines

import java.io.File

import mains.vimtag.{Change, Common, Empty, Keep, Parser}
import org.scalatest.FreeSpec

import common.test.AuxSpecs

class LinesParserTest extends FreeSpec with AuxSpecs {
  private val $ = new Parser(LinesParser)
  "apply" - {
    "Missing mandatory field throws" in {
      val lines = Vector(
        "ALBUM: Prokofiev - Piano Concerto No. 2 & 3 (Kissin, Ashkenazy)",
        "YEAR: <KEEP>",
        "COMPOSER: Prokofiev",
        "OPUS:",
        "CONDUCTOR: <KEEP>",
        "ORCHESTRA: Philharmonia Orchestra",
        "PERFORMANCEYEAR: 2009",
        "FILE: File1",
        "TITLE: Piano Concerto No.3 in C Major, Op.26 - I. Andante - Allegro",
        "TRACK: 5",
        "DISC_NO:",
        "FILE: File2",
        "TITLE: Piano Concerto No.3 in C Major, Op.26 - II. Tema & Variation",
        "TRACK: 6",
        "DISC_NO: Foobar",
        "FILE: File4",
        "TITLE: Piano Concerto No.3 in C Major, Op.26 - III. Allegro, ma non troppo",
        "TRACK: 9",
        "DISC_NO:",
      )
      val e = intercept[NoSuchElementException] {$(Common.dummyInitialValuesMap)(lines)}
      e.getMessage shouldReturn "key not found: ARTIST"
    }
    "Missing mandatory field in file throws" in {
      val lines = Vector(
        "ARTIST: Evgeny Kissin",
        "ALBUM: Prokofiev - Piano Concerto No. 2 & 3 (Kissin, Ashkenazy)",
        "YEAR: <KEEP>",
        "COMPOSER: Prokofiev",
        "OPUS:",
        "CONDUCTOR: <KEEP>",
        "ORCHESTRA: Philharmonia Orchestra",
        "PERFORMANCEYEAR: 2009",
        "FILE: File1",
        "TITLE: Piano Concerto No.3 in C Major, Op.26 - I. Andante - Allegro",
        "TRACK: 5",
        "DISC_NO:",
        "FILE: File2",
        "TRACK: 6",
        "DISC_NO: Foobar",
        "FILE: File4",
        "TITLE: Piano Concerto No.3 in C Major, Op.26 - III. Allegro, ma non troppo",
        "TRACK: 9",
        "DISC_NO:",
      )
      val e = intercept[NoSuchElementException] {$(Common.dummyInitialValuesMap)(lines)}
      e.getMessage shouldReturn "key not found for File2: TITLE"
    }
    "happy path" in {
      val lines = Vector(
        "ARTIST: Evgeny Kissin",
        "ALBUM: Prokofiev - Piano Concerto No. 2 & 3 (Kissin, Ashkenazy)",
        "YEAR: <KEEP>",
        "COMPOSER: Prokofiev",
        "OPUS: <COMMON>",
        "CONDUCTOR: <KEEP>",
        "ORCHESTRA: ",
        "PERFORMANCEYEAR: 2009",
        "FILE: File1",
        "TITLE: Piano Concerto No.3 in C Major, Op.26 - I. Andante - Allegro",
        "TRACK: 5",
        "DISC_NO:",
        "FILE: File2",
        "TITLE: Piano Concerto No.3 in C Major, Op.26 - II. Tema & Variation",
        "TRACK: 6",
        "DISC_NO: Foobar",
        "FILE: File4",
        "TITLE: Piano Concerto No.3 in C Major, Op.26 - III. Allegro, ma non troppo",
        "TRACK: 9",
        "DISC_NO:",
      )

      val res = $(Common.dummyWithOpus)(lines)

      res.artist shouldReturn Change("Evgeny Kissin")
      res.album shouldReturn Change("Prokofiev - Piano Concerto No. 2 & 3 (Kissin, Ashkenazy)")
      res.year shouldReturn Keep

      res.composer shouldReturn Change("Prokofiev")
      res.opus shouldReturn Change("Op. 14")
      res.conductor shouldReturn Keep
      res.orchestra shouldReturn Empty
      res.performanceYear shouldReturn Change(2009)

      val songs = res.songId3s
      val song1 = songs(0)
      song1.file shouldReturn new File("File1")
      song1.title shouldReturn "Piano Concerto No.3 in C Major, Op.26 - I. Andante - Allegro"
      song1.track shouldReturn 5
      song1.discNumber shouldReturn None

      val song2 = songs(1)
      song2.file shouldReturn new File("File2")
      song2.title shouldReturn "Piano Concerto No.3 in C Major, Op.26 - II. Tema & Variation"
      song2.track shouldReturn 6
      song2.discNumber.get shouldReturn "Foobar"

      val song3 = songs(2)
      song3.file shouldReturn new File("File4")
      song3.title shouldReturn "Piano Concerto No.3 in C Major, Op.26 - III. Allegro, ma non troppo"
      song3.track shouldReturn 9
      song3.discNumber shouldReturn None
    }
  }
}
