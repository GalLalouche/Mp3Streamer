package backend.albums

import java.time.{Clock, LocalDate}
import java.time.format.DateTimeFormatter

import backend.mb.{AlbumType, MbAlbumMetadata}
import backend.recon.{Album, Artist}
import mains.fixer.StringFixer
import play.api.libs.json.{Json, JsValue}

import scala.Ordering.Implicits._

import monocle.macros.Lenses

import common.json.Jsonable
import common.json.RichJson._
import common.rich.RichTime.{OrderingLocalDate, RichClock}
import common.rich.primitives.RichBoolean._

@Lenses
private case class NewAlbum(title: String, date: LocalDate, artist: Artist, albumType: AlbumType) {
  def isReleased(clock: Clock) = date <= clock.getLocalDate

  assert(StringFixer.SpecialQuotes.matcher(title).find().isFalse, title)
  assert(StringFixer.SpecialApostrophes.matcher(title).find().isFalse, title)
  def toAlbum: Album = Album(title = title, year = date.getYear, artist = artist)
}

private object NewAlbum {
  def from(a: Artist, mb: MbAlbumMetadata): NewAlbum =
    NewAlbum(mb.title, mb.releaseDate, a, mb.albumType)

  implicit object NewAlbumJsonable extends Jsonable[NewAlbum] {
    private val DateFormat = DateTimeFormatter.ofPattern("yyyy/MM/dd")
    override def jsonify(a: NewAlbum) = Json.obj(
      "title" -> a.title,
      "date" -> DateFormat.format(a.date),
      "artistName" -> a.artist.name,
      "albumType" -> a.albumType.toString,
    )
    override def parse(json: JsValue) = NewAlbum(
      title = json str "title",
      date = LocalDate.from(DateFormat.parse(json str "date")),
      artist = Artist(json str "artistName"),
      albumType = AlbumType.withName(json str "albumType"),
    )
  }
}
