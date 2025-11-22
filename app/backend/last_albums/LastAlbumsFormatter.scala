package backend.last_albums

import backend.FutureOption
import com.google.inject.Inject
import models.ModelJsonable
import play.api.libs.json.JsValue

import scala.concurrent.{ExecutionContext, Future}

import common.json.ToJsonableOps._

class LastAlbumsFormatter @Inject() (
    ec: ExecutionContext,
    lastAlbumState: LastAlbumsState,
    mj: ModelJsonable,
) {
  import mj.albumDirJsonifier
  private implicit val iec: ExecutionContext = ec

  def updateLast(): Future[JsValue] = lastAlbumState.update().map(_.jsonify)
  def getLast: JsValue = lastAlbumState.get.jsonify
  def dequeueNextAlbum(): FutureOption[JsValue] = lastAlbumState.dequeue().map(_.jsonify)
}
