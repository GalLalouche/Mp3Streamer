package backend.albums

import backend.mb.MbAlbumMetadata
import backend.recon.{Album, Artist}
import play.api.libs.json.{Json, JsValue}

import monocle.macros.Lenses

import common.json.RichJson._
import common.json.Jsonable

@Lenses
private case class NewAlbum(title: String, year: Int, artist: Artist, albumType: AlbumType) {
  def toAlbum: Album = Album(title = title, year = year, artist = artist)
}

private object NewAlbum {
  def from(a: Artist, mb: MbAlbumMetadata): NewAlbum =
    NewAlbum(mb.title, mb.releaseDate.getYear, a, mb.albumType)

  implicit object NewAlbumJsonable extends Jsonable[NewAlbum] {
    override def jsonify(a: NewAlbum) = Json.obj(
      "title" -> a.title,
      "year" -> a.year,
      "artistName" -> a.artist.name,
      "albumType" -> a.albumType.toString,
    )
    override def parse(json: JsValue) = NewAlbum(
      title = json str "title",
      year = json int "year",
      artist = Artist(json str "artistName"),
      albumType = AlbumType.withName(json str "albumType"),
    )
  }
}
