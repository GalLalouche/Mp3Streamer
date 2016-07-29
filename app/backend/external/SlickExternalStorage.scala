package backend.external

import backend.recon.Reconcilable
import backend.storage.SlickLocalStorageUtils
import backend.{Configuration, StringSerializable, Url}
import common.rich.RichT._
import org.joda.time.DateTime

import scala.concurrent.Future

class SlickExternalStorage[K <: Reconcilable](implicit c: Configuration,
                                              m: Manifest[K]) extends ExternalStorage[K] {
  import c.driver.api._
  
  private object Serializable extends StringSerializable[ExternalLink[K]] {
    override def encode(e: ExternalLink[K]): String =
      s"${e.host.name};${e.host.url.address};${e.link.address}"
    override def decode(s: String): ExternalLink[K] =
      s.split(";").ensuring(_.length == 3).mapTo(e => ExternalLink[K](Url(e(2)), Host(e(0), Url(e(1)))))
  }
  
  private class Rows(tag: Tag) extends
      Table[(String, String, Option[Long])](tag, m.runtimeClass.getSimpleName.replaceAll("\\$", "").toUpperCase + "_LINKS") {
    def name = column[String]("KEY", O.PrimaryKey)
    def encodedLinks = column[String]("LINKS_STRING")
    def timestamp = column[Option[Long]]("TIMESTAMP")
    def * = (name, encodedLinks, timestamp)
  }
  private val rows = TableQuery[Rows]
  private val db = c.db
  
  private def toString(els: Links[K]): String = els map Serializable.encode mkString ";;"
  private def fromString(s: String): Links[K] = s split ";;" filterNot (_.isEmpty) map Serializable.decode
  private def store(k: K, els: Links[K], t: Option[Long]): Future[Unit] =
    db.run(rows.forceInsert((k.normalize, els |> toString, t))).map(e => Unit)
  override def load(k: K): Future[Option[(Links[K], Option[DateTime])]] =
    db.run(rows
               .filter(_.name === k.normalize)
               .map(e => e.encodedLinks -> e.timestamp)
               .result
               .map(_.headOption.map(_.mapTo(e => e._1.mapTo(fromString) -> e._2.map(new DateTime(_))))))
  override protected def internalForceStore(k: K, v: (Links[K], Option[DateTime])): Future[Unit] =
    store(k, v._1, v._2.map(_.getMillis))
  override def utils = SlickLocalStorageUtils(c)(rows)
}



