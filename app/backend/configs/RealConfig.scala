package backend.configs

import java.net.HttpURLConnection

import backend.Url
import common.io.{DirectoryRef, IODirectory, IOSystem}
import models.IOMusicFinder
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import slick.driver.{JdbcProfile, SQLiteDriver}

import scala.concurrent.Future
import scala.io.Source

trait RealConfig extends Configuration {
  override lazy implicit val driver: JdbcProfile = SQLiteDriver
  override type S = IOSystem
  override implicit lazy val db: driver.backend.DatabaseDef = driver.api.Database.forURL("jdbc:sqlite:d:/media/music/MBRecon.sqlite", driver = "org.sqlite.JDBC")
  override implicit lazy val mf: IOMusicFinder = IOMusicFinder
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
