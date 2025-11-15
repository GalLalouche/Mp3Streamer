package backend.mb

import java.time.LocalDate

import backend.recon.{Artist, ReconID}
import org.scalatest.Inside._
import org.scalatest.OptionValues._
import org.scalatest.freespec.AnyFreeSpec
import play.api.libs.json.{JsObject, Json}

import common.test.AuxSpecs

class AlbumParserTest extends AnyFreeSpec with AuxSpecs {
  private val $ = AlbumParser
  private def parse(s: String): Option[AlbumMetadata] =
    $.parseReleaseGroup(Json.parse(s.stripMargin).as[JsObject])
  "valid input" in {
    val result = parse("""{
        |"first-release-date": "2017-07-15",
        |"secondary-type-ids": [],
        |"id": "63c6deb8-3dcb-48e2-bf31-66d145398a33",
        |"disambiguation": "",
        |"primary-type-id": "f529b476-6e62-324f-b0aa-1f3e33d313fc",
        |"primary-type": "Album",
        |"title": "ניתוקים",
        |"secondary-types": []
        |}""").value
    inside(result) { case AlbumMetadata(title, releaseDate, albumType, reconId, disambiguation) =>
      title shouldReturn "ניתוקים"
      releaseDate shouldReturn LocalDate.of(2017, 7, 15)
      albumType shouldReturn AlbumType.Album
      reconId shouldReturn ReconID("63c6deb8-3dcb-48e2-bf31-66d145398a33")
      disambiguation shouldReturn None
    }
  }

  "valid input with disambiguation" in {
    val result = parse("""{
                         |"first-release-date": "2017-07-15",
                         |"secondary-type-ids": [],
                         |"id": "63c6deb8-3dcb-48e2-bf31-66d145398a33",
                         |"disambiguation": "foobar",
                         |"primary-type-id": "f529b476-6e62-324f-b0aa-1f3e33d313fc",
                         |"primary-type": "Album",
                         |"title": "ניתוקים",
                         |"secondary-types": []
                         |}""").value
    inside(result) { case AlbumMetadata(title, releaseDate, albumType, reconId, disambiguation) =>
      title shouldReturn "ניתוקים"
      releaseDate shouldReturn LocalDate.of(2017, 7, 15)
      albumType shouldReturn AlbumType.Album
      reconId shouldReturn ReconID("63c6deb8-3dcb-48e2-bf31-66d145398a33")
      disambiguation.value shouldReturn "foobar"
    }
  }

  "Ignores secondary types" in {
    parse("""{
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
    parse("""{
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

  "releaseGroup parsing" in {
    $.releaseGroups(Json.parse(getClass.getResourceAsStream("release-group.json")))
      .shouldContainExactly(
        AlbumMetadata(
          "O",
          LocalDate.of(2002, 7, 22),
          AlbumType.Album,
          ReconID("d7e69fd9-59ac-3093-8b72-60b77a91b298"),
          None,
        ),
        AlbumMetadata(
          "9",
          LocalDate.of(2006, 11, 6),
          AlbumType.Album,
          ReconID("0d673e32-4f95-348f-af28-3abc1353bff3"),
          None,
        ),
        AlbumMetadata(
          "My Favourite Faded Fantasy",
          LocalDate.of(2014, 10, 31),
          AlbumType.Album,
          ReconID("72ea557d-b39d-4e06-bb17-e3bda5802d4b"),
          None,
        ),
        AlbumMetadata(
          "Live From the Union Chapel",
          LocalDate.of(2003, 7, 1),
          AlbumType.Live,
          ReconID("076a1fb6-c1da-38dc-8bc0-1b7f4c2256f7"),
          None,
        ),
        AlbumMetadata(
          "2004 Live at Outremont Theatre, Montreal",
          LocalDate.of(2004, 1, 1),
          AlbumType.Live,
          ReconID("0b2d6869-bc7b-4b8a-bc04-03d73f279831"),
          None,
        ),
        AlbumMetadata(
          "Live at Fingerprints: Warts and All",
          LocalDate.of(2007, 10, 23),
          AlbumType.EP,
          ReconID("bd7b0573-0b62-3026-afdc-e5f2e12cfe61"),
          None,
        ),
      )
  }

  "releaseToReleaseGroups" - {
    "ignores singles, concatenates repeats" in {
      $.releaseToReleaseGroups(Json.parse(getClass.getResourceAsStream("release.json")))
        .shouldContainExactly(
          AlbumMetadata(
            "Olden Tales & Deathly Trails",
            LocalDate.of(2012, 9, 21),
            AlbumType.Album,
            ReconID("f3098f4d-7a46-46a9-85cf-c3e69d1398ea"),
            None,
          ),
          AlbumMetadata(
            "Sleep at the Edge of the Earth",
            LocalDate.of(2015, 4, 7),
            AlbumType.Album,
            ReconID("afe8a3e0-96bc-4a63-8be1-3e133ad4f702"),
            None,
          ),
          AlbumMetadata(
            "Veil of Imagination",
            LocalDate.of(2019, 11, 1),
            AlbumType.Album,
            ReconID("82b1bdf5-6b64-4580-82d2-32f2a9af5321"),
            None,
          ),
        )
    }
    "Handles repeats in the same date by most popular" in {
      $.releaseToReleaseGroups(Json.parse(getClass.getResourceAsStream("release_repeats_pop.json")))
        .shouldContainExactly(
          AlbumMetadata(
            "Triumphant Hearts",
            LocalDate.of(2018, 12, 7),
            AlbumType.Album,
            ReconID("900e1bfb-beb2-4a36-b5ab-c8af11fc1dc4"),
            None,
          ),
        )
    }
    "Handles repeats with different dates by earliest" in {
      $.releaseToReleaseGroups(
        Json.parse(getClass.getResourceAsStream("release_repeats_date.json")),
      ).shouldContainExactly(
        AlbumMetadata(
          "Out",
          LocalDate.of(2006, 8, 1),
          AlbumType.Album,
          ReconID("7ef02c74-773f-3a22-beed-b227a86e7e34"),
          None,
        ),
      )
    }
  }

  "artistCredits" in {
    $.artistCredits(
      Json.parse(getClass.getResourceAsStream("split.json")).asInstanceOf[JsObject],
    ).shouldContainExactly(
      Artist("Baroness") -> ReconID("eeb41a1e-4326-4d04-8c47-0f564ceecd68"),
      Artist("Unpersons") -> ReconID("8b43a9eb-4f63-4425-bef1-cf2b068c00c9"),
    )
  }
}
