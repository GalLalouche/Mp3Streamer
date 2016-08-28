package backend

import java.net.HttpURLConnection

import common.io.{DirectoryRef, IODirectory}
import models.{MusicFinder, RealLocations}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import slick.driver.{JdbcProfile, SQLiteDriver}

import scala.concurrent.Future
import scala.io.Source

trait RealConfig extends Configuration {
  override lazy implicit val driver: JdbcProfile = SQLiteDriver
  override implicit val db: driver.backend.DatabaseDef = driver.api.Database.forURL("jdbc:sqlite:d:/media/music/MBRecon.sqlite", driver = "org.sqlite.JDBC")
  override implicit lazy val mf: MusicFinder = RealLocations
  override def downloadDocument(url: Url): Future[Document] =
    Future(Source.fromURL(url.address, "UTF-8"))
        .map(_.mkString)
        .map(Jsoup parse)
  override def connect(http: HttpURLConnection): Future[HttpURLConnection] = Future {
    http.connect()
    http
  }
  override implicit lazy val rootDirectory: DirectoryRef = IODirectory.apply("D:/media/streamer/")
}
