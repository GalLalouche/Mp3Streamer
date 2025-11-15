package backend.new_albums

import java.time.{Clock, LocalDate}
import java.time.format.DateTimeFormatter

import backend.mb.{AlbumMetadata, AlbumType}
import backend.recon.{Album, Artist, ReconID}
import mains.fixer.StringFixer
import models.TypeAliases.AlbumTitle
import play.api.libs.json.{JsObject, Json}

import scala.Ordering.Implicits._

import monocle.macros.Lenses

import common.json.OJsonable
import common.json.RichJson._
import common.rich.RichTime.{OrderingLocalDate, RichClock}
import common.rich.primitives.RichBoolean._

@Lenses
case class NewAlbum(
    title: AlbumTitle,
    date: LocalDate,
    artist: Artist,
    albumType: AlbumType,
    reconID: ReconID,
) {
  def isReleased(clock: Clock): Boolean = date <= clock.getLocalDate

  assert(StringFixer.SpecialQuotes.matcher(title).find().isFalse, title)
  assert(StringFixer.SpecialApostrophes.matcher(title).find().isFalse, title)
  def toAlbum: Album = Album(title = title, year = date.getYear, artist = artist)
}

private object NewAlbum {
  def from(a: Artist, mb: AlbumMetadata): NewAlbum =
    NewAlbum(mb.title, mb.releaseDate, a, mb.albumType, mb.reconId)

  implicit object NewAlbumJsonable extends OJsonable[NewAlbum] {
    override def jsonify(a: NewAlbum) = Json.obj(
      "title" -> a.title,
      "date" -> DateFormat.format(a.date),
      "artistName" -> a.artist.name,
      "albumType" -> a.albumType.toString,
      "reconID" -> a.reconID.id,
    )
    override def parse(json: JsObject) = NewAlbum(
      title = json.str("title"),
      date = LocalDate.from(DateFormat.parse(json.str("date"))),
      artist = Artist(json.str("artistName")),
      albumType = AlbumType.withName(json.str("albumType")),
      reconID = ReconID.validateOrThrow(json.str("reconID")),
    )

    private val DateFormat = DateTimeFormatter.ofPattern("yyyy/MM/dd")
  }
}
