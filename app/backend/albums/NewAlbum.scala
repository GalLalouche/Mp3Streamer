package backend.albums

import backend.mb.MbArtistReconciler.MbAlbumMetadata
import backend.recon.{Album, Artist}
import common.RichJson._
import common.json.Jsonable
import monocle.macros.Lenses
import play.api.libs.json.{JsValue, Json}

// Has to be public in order to use @Lenses
@Lenses
case class NewAlbum(title: String, year: Int, artist: Artist, albumType: NewAlbum.AlbumType.AlbumType) {
  def toAlbum: Album = Album(title = title, year = year, artist = artist)
}

object NewAlbum {
  def from(a: Artist, mb: MbAlbumMetadata): NewAlbum = NewAlbum(mb.title, mb.releaseDate.getYear, a, mb.albumType)

  object AlbumType extends Enumeration {
    type AlbumType = Value
    val EP, Album, Live, Compilation = Value
  }

  implicit object NewAlbumJsonable extends Jsonable[NewAlbum] {
    override def jsonify(a: NewAlbum) =
      Json.obj("title" -> a.title, "year" -> a.year, "artistName" -> a.artist.name, "albumType" -> a.albumType.toString)
    override def parse(json: JsValue) = NewAlbum(
      title = json str "title",
      year = json int "year",
      artist = Artist(json str "artistName"),
      albumType = AlbumType.withName(json str "albumType"))
  }
}

