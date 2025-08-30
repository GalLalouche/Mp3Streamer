package playlist

import com.google.inject.Inject
import models.{ModelJsonable, Song}
import play.api.libs.json.JsObject
import playlist.PlaylistModel._

import scala.concurrent.{ExecutionContext, Future}

import common.rich.func.MoreMapInstances.basicMonoid
import common.rich.func.ToMoreMonoidOps.toMoreMonoidOptionOps

import common.io.JsonableSaver
import common.json.{Jsonable, OJsonable}
import common.json.OJsonable.MapJsonable
import common.json.ToJsonableOps._
import common.rich.RichT.richT

private class PlaylistModel @Inject() (
    ec: ExecutionContext,
    saver: JsonableSaver,
    mj: ModelJsonable,
) {
  private implicit val iec: ExecutionContext = ec
  import mj.songJsonifier
  def getIds: Future[Set[String]] = loadOrZero.map(_.keys.toSet)
  def get(id: String): Future[Option[Playlist]] = loadOrZero.map(_.get(id))
  def set(id: String, value: Playlist): Future[Unit] = modify(e => ((), e + (id -> value)))
  def remove(id: String): Future[Boolean] = modify(_.toTuple(_.contains(id), _ - id))

  private def loadOrZero: Future[State] =
    Future(saver.loadObjectOpt[PlaylistModel.PlaylistMap].map(_.s).getOrZero)
  private def modify[A](f: State => (A, State)): Future[A] = loadOrZero.map { s =>
    val (result, newState) = f(s)
    saver.saveObject(PlaylistMap(newState))
    result
  }
}

private object PlaylistModel {
  // For the JsonSaver file name.
  private case class PlaylistMap(s: Map[String, Playlist])
  private implicit def autoPlaylistJsonable(implicit sj: Jsonable[Song]): OJsonable[PlaylistMap] =
    new OJsonable[PlaylistMap] {
      override def jsonify(a: PlaylistMap): JsObject = a.s.ojsonify
      override def parse(json: JsObject): PlaylistMap = PlaylistMap(json.parse[State])
    }
  private type State = Map[String, Playlist]
}
