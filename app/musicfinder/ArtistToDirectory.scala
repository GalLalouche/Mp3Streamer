package musicfinder

import backend.recon.Artist
import models.ArtistDir
import play.api.libs.json.{JsArray, JsString, JsValue}

import common.io.{DirectoryRef, IODirectory}
import common.json.Jsonable
import common.json.ToJsonableOps.jsonifyArray

// Much faster to parse and jsonify than ArtistDir.
private case class ArtistToDirectory(artist: Artist, dir: DirectoryRef)
private object ArtistToDirectory {
  implicit object ArtistToDirectoryJsonable extends Jsonable[ArtistToDirectory] {
    override def jsonify(e: ArtistToDirectory): JsValue =
      Vector(e.artist.name, e.dir.path).jsonifyArray
    override def parse(json: JsValue): ArtistToDirectory = json match {
      case JsArray(value) =>
        value.toVector match {
          case Vector(JsString(name), JsString(path)) =>
            ArtistToDirectory(Artist(name), IODirectory(path))
        }
    }
  }
  def from(artistDir: ArtistDir): ArtistToDirectory =
    ArtistToDirectory(artistDir.toRecon, artistDir.dir)
}
