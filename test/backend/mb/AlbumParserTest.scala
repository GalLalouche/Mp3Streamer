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
  private def parse(s: String): Option[MbAlbumMetadata] =
    new AlbumParser(Logger.Empty)(Json.parse(s.stripMargin).as[JsObject])
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
}
