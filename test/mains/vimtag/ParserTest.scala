package mains.vimtag

import java.io.File

import common.AuxSpecs
import org.scalatest.FreeSpec

class ParserTest extends FreeSpec with AuxSpecs {
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
      val e = intercept[NoSuchElementException] {Parser(lines)}
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
      val e = intercept[NoSuchElementException] {Parser(lines)}
      e.getMessage shouldReturn "key not found for File2: TITLE"
    }
    "happy path" in {
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
        "TITLE: Piano Concerto No.3 in C Major, Op.26 - II. Tema & Variation",
        "TRACK: 6",
        "DISC_NO: Foobar",
        "FILE: File4",
        "TITLE: Piano Concerto No.3 in C Major, Op.26 - III. Allegro, ma non troppo",
        "TRACK: 9",
        "DISC_NO:",
      )

      val $ = Parser(lines)

      $.artist shouldReturn Change("Evgeny Kissin")
      $.album shouldReturn Change("Prokofiev - Piano Concerto No. 2 & 3 (Kissin, Ashkenazy)")
      $.year shouldReturn Keep

      $.composer shouldReturn Change("Prokofiev")
      $.opus shouldReturn Empty
      $.conductor shouldReturn Keep
      $.orchestra shouldReturn Change("Philharmonia Orchestra")
      $.performanceYear shouldReturn Change(2009)

      val songs = $.songId3s
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
