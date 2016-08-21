package backend
import java.net.{HttpURLConnection, URL}

import models.{MusicFinder, RealLocations}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import slick.driver.{JdbcProfile, SQLiteDriver}

import scala.concurrent.Future
import scala.io.Source

trait RealConfig extends Configuration {
  override implicit val driver: JdbcProfile = SQLiteDriver
  override implicit val db = driver.api.Database.forURL("jdbc:sqlite:d:/media/music/MBRecon.sqlite", driver = "org.sqlite.JDBC")
  override implicit lazy val mf: MusicFinder = RealLocations
  override def downloadDocument(url: Url): Future[Document] =
    Future(Source.fromURL(url.address, "UTF-8"))
        .map(_.mkString)
        .map(Jsoup parse)
  override def httpUrlConnection(url: Url, modify: (HttpURLConnection) => Unit): Future[HttpURLConnection] =
    Future(new URL(url.address).openConnection())
        .map(_.asInstanceOf[HttpURLConnection])
        .map(e => {
          modify(e)
          e
        })
}
