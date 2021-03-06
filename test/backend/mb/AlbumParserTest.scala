package backend.mb

import java.time.LocalDate

import backend.logging.Logger
import backend.recon.ReconID
import org.scalatest.FreeSpec
import org.scalatest.Inside._
import org.scalatest.OptionValues._
import play.api.libs.json.{JsObject, Json}

import common.test.AuxSpecs

class AlbumParserTest extends FreeSpec with AuxSpecs {
  private val $ = new AlbumParser(Logger.Empty)
  private def parse(s: String): Option[MbAlbumMetadata] =
    $(Json.parse(s.stripMargin).as[JsObject])
  "valid input" in {
    val result = parse(
      """{
        |"first-release-date": "2017-07-15",
        |"secondary-type-ids": [],
        |"id": "63c6deb8-3dcb-48e2-bf31-66d145398a33",
        |"disambiguation": "",
        |"primary-type-id": "f529b476-6e62-324f-b0aa-1f3e33d313fc",
        |"primary-type": "Album",
        |"title": "ניתוקים",
        |"secondary-types": []
        |}""").value
    inside(result) {case MbAlbumMetadata(title, releaseDate, albumType, reconId) =>
      title shouldReturn "ניתוקים"
      releaseDate shouldReturn LocalDate.of(2017, 7, 15)
      albumType shouldReturn AlbumType.Album
      reconId shouldReturn ReconID("63c6deb8-3dcb-48e2-bf31-66d145398a33")
    }
  }

  "Ignores secondary types" in {
    parse(
      """{
        |"secondary-type-ids": [
        |"dd2a21e1-0c00-3729-a7a0-de60b84eb5d1"
        |],
        |"title": "Deliverance & Damnation",
        |"secondary-types": [
        |"Compilation"
        |],
        |"disambiguation": "",
        |"id": "f1cb012c-81c7-47bb-b5f2-7a85c399c029",
        |"first-release-date": "2015-10-30",
        |"primary-type": "Album",
        |"primary-type-id": "f529b476-6e62-324f-b0aa-1f3e33d313fc"
        |}""") shouldReturn None
  }

  "Returns none on invalid date" in {
    parse(
      """{
        |"id": "e96e6015-e6b2-4af6-88e2-d24ff79d18a5",
        |"secondary-type-ids": [],
        |"primary-type-id": "f529b476-6e62-324f-b0aa-1f3e33d313fc",
        |"disambiguation": "",
        |"primary-type": "Album",
        |"secondary-types": [],
        |"first-release-date": "2011-??-12",
        |"title": "London or Paris, Berlin or Southend On Sea"
        |}""") shouldReturn None
  }

  "releaseToReleaseGroups" - {
    "ignores singles, concatantes repeats" in {
      $.releaseToReleaseGroups(Json.parse(getClass.getResourceAsStream("release.json"))).shouldContainExactly(
        MbAlbumMetadata("Olden Tales & Deathly Trails", LocalDate.of(2012, 9, 21), AlbumType.Album, ReconID("f3098f4d-7a46-46a9-85cf-c3e69d1398ea")),
        MbAlbumMetadata("Sleep at the Edge of the Earth", LocalDate.of(2015, 4, 7), AlbumType.Album, ReconID("afe8a3e0-96bc-4a63-8be1-3e133ad4f702")),
        MbAlbumMetadata("Veil of Imagination", LocalDate.of(2019, 11, 1), AlbumType.Album, ReconID("82b1bdf5-6b64-4580-82d2-32f2a9af5321")),
      )
    }
    "Handles repeats in the same date by most popular" in {
      $.releaseToReleaseGroups(Json.parse(getClass.getResourceAsStream("release_repeats_pop.json"))).shouldContainExactly(
        MbAlbumMetadata("Triumphant Hearts", LocalDate.of(2018, 12, 7), AlbumType.Album, ReconID("900e1bfb-beb2-4a36-b5ab-c8af11fc1dc4")),
      )
    }
    "Handles repeats with different dates by earliest" in {
      $.releaseToReleaseGroups(Json.parse(getClass.getResourceAsStream("release_repeats_date.json"))).shouldContainExactly(
        MbAlbumMetadata("Out", LocalDate.of(2006, 8, 1), AlbumType.Album, ReconID("7ef02c74-773f-3a22-beed-b227a86e7e34")),
      )
    }
  }
}
