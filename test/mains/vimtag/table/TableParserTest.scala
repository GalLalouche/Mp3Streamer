package mains.vimtag.table

import mains.vimtag.{Change, Common, Empty, Keep, Parser}
import org.scalatest.FreeSpec

import common.test.AuxSpecs

class TableParserTest extends FreeSpec with AuxSpecs {
  private val $ = new Parser(TableParser)
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
        "ALBUM: Piano Concerto No.3",
        "YEAR: 2015",
        "|---------+---------------------------------------------------------------------+-------------+-------|",
        "| Track # | Title                                                               | Disc Number | File  |",
        "|---------+---------------------------------------------------------------------+-------------+-------|",
        "| 1       | Piano Concerto No.3 in C Major, Op.26 - I. Andante - Allegro        |             | File1 |",
        "| 2       | Piano Concerto No.3 in C Major, Op.26 - II. Tema & Variation        |             | File2 |",
        "| 3       | Piano Concerto No.3 in C Major, Op.26 - III. Allegro, ma non troppo |             | File3 |",
        "|---------+---------------------------------------------------------------------+-------------+-------|",
      )
      val e = intercept[NoSuchElementException]($(Common.dummyInitialValuesMap)(lines))
      e.getMessage shouldReturn "key not found: ARTIST"
    }
    "Empty mandatory cell throws" in {
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
        "ALBUM: Ulvesang",
        "YEAR: 2015",
        "|---------+---------------------------------------------------------------------+-------------+-------|",
        "| Track # | Title                                                               | Disc Number | File  |",
        "|---------+---------------------------------------------------------------------+-------------+-------|",
        "| 1       | Piano Concerto No.3 in C Major, Op.26 - I. Andante - Allegro        |             | File1 |",
        "| 2       |                                                                     |             | File2 |",
        "| 3       | Piano Concerto No.3 in C Major, Op.26 - III. Allegro, ma non troppo |             | File3 |",
        "|---------+---------------------------------------------------------------------+-------------+-------|",
      )
      val e = intercept[NoSuchElementException]($(Common.dummyInitialValuesMap)(lines))
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
        "|---------+---------------------------------------------------------------------+-------------+-------|",
        "| Track # | Title                                                               | Disc Number | File  |",
        "|---------+---------------------------------------------------------------------+-------------+-------|",
        "| 5       | Piano Concerto No.3 in C Major, Op.26 - I. Andante - Allegro        |             | File1 |",
        "| 6       | Piano Concerto No.3 in C Major, Op.26 - II. Tema & Variation        | Foobar      | File2 |",
        "| 9       | Piano Concerto No.3 in C Major, Op.26 - III. Allegro, ma non troppo |             | File4 |",
        "|---------+---------------------------------------------------------------------+-------------+-------|",
      )

      val res = $(Common.dummyInitialValuesMap)(lines)

      res.artist shouldReturn Change("Evgeny Kissin")
      res.album shouldReturn Change("Prokofiev - Piano Concerto No. 2 & 3 (Kissin, Ashkenazy)")
      res.year shouldReturn Keep

      res.composer shouldReturn Change("Prokofiev")
      res.opus shouldReturn Empty
      res.conductor shouldReturn Keep
      res.orchestra shouldReturn Change("Philharmonia Orchestra")
      res.performanceYear shouldReturn Change(2009)

      val songs = res.songId3s
      val song1 = songs(0)
      song1.relativeFileName shouldReturn "File1"
      song1.title shouldReturn "Piano Concerto No.3 in C Major, Op.26 - I. Andante - Allegro"
      song1.track shouldReturn 5
      song1.discNumber shouldReturn None

      val song2 = songs(1)
      song2.relativeFileName shouldReturn "File2"
      song2.title shouldReturn "Piano Concerto No.3 in C Major, Op.26 - II. Tema & Variation"
      song2.track shouldReturn 6
      song2.discNumber.get shouldReturn "Foobar"

      val song3 = songs(2)
      song3.relativeFileName shouldReturn "File4"
      song3.title shouldReturn "Piano Concerto No.3 in C Major, Op.26 - III. Allegro, ma non troppo"
      song3.track shouldReturn 9
      song3.discNumber shouldReturn None
    }
    "Parses special bonus directive" in {
      val lines = Vector(
        "ARTIST: Karajan",
        "ALBUM: Symphonie Fantastique",
        "YEAR: 1830",
        "COMPOSER: Berlioz",
        "OPUS: <COMMON>",
        "CONDUCTOR:",
        "ORCHESTRA: Berliner Philharmoniker",
        "PERFORMANCEYEAR:",
        "|---------+---------------------------------+-------------+-------|",
        "| Track # | Title                           | Disc Number | File  |",
        "|---------+---------------------------------+-------------+-------|",
        "| 1       | I Reveries and Passions         |             | File1 |",
        "| 2       | II A Ball                       | Foobar ->   | File2 |",
        "| 3       | III In the Country              |             | File3 |",
        "| 4       | IV March to the Scaffold        | Moo         | File4 |",
        "| 5       | V Dream of the Witches' Sabbath |             | File5 |",
        "---------+---------------------------------+-------------+--------|",
      )

      val res = $(Common.dummyWithOpus)(lines)

      res.artist shouldReturn Change("Karajan")
      res.album shouldReturn Change("Symphonie Fantastique")
      res.year shouldReturn Change(1830)

      res.composer shouldReturn Change("Berlioz")
      res.opus shouldReturn Change("Op. 14")
      res.conductor shouldReturn Empty
      res.orchestra shouldReturn Change("Berliner Philharmoniker")
      res.performanceYear shouldReturn Empty

      val songs = res.songId3s
      val song1 = songs(0)
      song1.relativeFileName shouldReturn "File1"
      song1.title shouldReturn "I Reveries and Passions"
      song1.track shouldReturn 1
      song1.discNumber shouldReturn None

      val song2 = songs(1)
      song2.relativeFileName shouldReturn "File2"
      song2.title shouldReturn "II A Ball"
      song2.track shouldReturn 2
      song2.discNumber.get shouldReturn "Foobar"

      val song3 = songs(2)
      song3.relativeFileName shouldReturn "File3"
      song3.title shouldReturn "III In the Country"
      song3.track shouldReturn 3
      song3.discNumber.get shouldReturn "Foobar"

      val song4 = songs(3)
      song4.relativeFileName shouldReturn "File4"
      song4.title shouldReturn "IV March to the Scaffold"
      song4.track shouldReturn 4
      song4.discNumber.get shouldReturn "Moo"

      val song5 = songs(4)
      song5.relativeFileName shouldReturn "File5"
      song5.title shouldReturn "V Dream of the Witches' Sabbath"
      song5.track shouldReturn 5
      song5.discNumber shouldReturn None
    }
  }
}
