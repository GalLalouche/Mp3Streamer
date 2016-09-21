package playlist


import models.Song
import scala.concurrent.duration.Duration

case class PlaylistClone(songs: Seq[Song], currentIndex: Int, currentDuration: Duration) {
  require(currentIndex < songs.length && currentIndex >= 0, s"currentIndex <$currentIndex> out of range (0-${songs.length})")
  require(currentDuration != null)
}
object PlaylistClone {

  import common.Jsonable
  import play.api.libs.json.{JsObject, Json}
  import search.ModelsJsonable.SongJsonifier
  import common.RichJson._
  import common.rich.RichT._

  implicit object PlaylistCloneJsonable extends Jsonable[PlaylistClone] {
    override def jsonify(t: PlaylistClone): JsObject = Json.obj(
      "songs" -> SongJsonifier.jsonify(t.songs),
      "duration" -> t.currentDuration.toSeconds,
      "currentIndex" -> t.currentIndex)
    override def parse(json: JsObject): PlaylistClone = PlaylistClone(
      songs = json array "songs" mapTo SongJsonifier.parse,
      currentIndex = json / "currentIndex",
      currentDuration = Duration.apply(json / "duration"))
  }
}

