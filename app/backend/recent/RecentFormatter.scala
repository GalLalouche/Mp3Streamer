package backend.recent

import javax.inject.Inject

import models.ModelJsonable.AlbumDirJsonifier
import play.api.libs.json.JsValue

import scala.concurrent.{ExecutionContext, Future}

import common.json.ToJsonableOps._
import common.rich.collections.RichTraversableOnce.richTraversableOnce

class RecentFormatter @Inject() (ec: ExecutionContext, recentAlbums: RecentAlbums) {
  private def sinceDays(d: Int): Future[JsValue] = Future(recentAlbums.sinceDays(d)).map(_.jsonify)
  private def sinceMonths(m: Int): Future[JsValue] =
    Future(recentAlbums.sinceMonths(m)).map(_.jsonify)
  def since(dayString: String): Future[JsValue] = {
    val number = dayString.takeWhile(_.isDigit).toInt
    val last = dayString.last.toLower
    if (last == 'd' || last.isDigit)
      sinceDays(number)
    else {
      require(last == 'm', "Formats support are pure numbers, numbers ending in d, or ending in m")
      sinceMonths(number)
    }
  }

  private implicit val iec: ExecutionContext = ec

  def all(amount: Int): Future[JsValue] = Future(recentAlbums.all(amount)).map(_.jsonify)
  def double(amount: Int): Future[JsValue] = Future(recentAlbums.double(amount)).map(_.jsonify)
  def last: Future[JsValue] = Future(recentAlbums.all(1).single.jsonify)
}
