package server

import backend.lyrics.retrievers.InstrumentalArtistStorage
import backend.recon.ArtistReconStorage
import backend.storage.DbProvider
import com.google.inject.Module
import models.IOSong
import net.codingwell.scalaguice.InjectorExtensions.ScalaInjector
import sttp.client3.UriContext

import scala.concurrent.Future

import cats.implicits.catsSyntaxFlatMapOps

import common.test.BeforeAndAfterEachAsync

private class LyricsTest(serverModule: Module)
    extends HttpServerSpecs(serverModule)
    with BeforeAndAfterEachAsync {

  private val file = getResourceFile("/models/song.mp3")
  private val song = IOSong.read(file)
  // Must use a relative path because Http4sUtils.decodePath strips the leading '/'.
  private val songPath =
    new java.io.File(".").getCanonicalFile.toPath.relativize(file.toPath).toString

  private val artistReconStorage = injector.instance[ArtistReconStorage]
  private val instrumentalArtistStorage = injector.instance[InstrumentalArtistStorage]
  private val dbProvider = injector.instance[DbProvider]

  import dbProvider.profile.api._

  private def createLyricsTable: Future[Unit] = dbProvider.db.run(DBIO.seq(
    sqlu"""CREATE TABLE IF NOT EXISTS "lyrics" (
      "song" VARCHAR NOT NULL PRIMARY KEY,
      "source" VARCHAR NOT NULL,
      "lyrics" VARCHAR,
      "url" VARCHAR)""",
  ))

  protected override def beforeEach(): Future[_] =
    artistReconStorage.utils.clearOrCreateTable() >>
      instrumentalArtistStorage.utils.clearOrCreateTable() >>
      createLyricsTable

  "GET returns failure message when no lyrics are found" in {
    getString(uri"lyrics/$songPath") shouldEventuallyReturn "Failed to get lyrics :("
  }

  "POST instrumental/song marks song as instrumental" in {
    postString(uri"lyrics/instrumental/song/$songPath").map { result =>
      result should include("<b>Instrumental</b>")
      result should include("Manual override")
    }
  }

  "GET returns cached instrumental after setting song as instrumental" in {
    for {
      _ <- postString(uri"lyrics/instrumental/song/$songPath")
      result <- getString(uri"lyrics/$songPath")
    } yield {
      result should include("<b>Instrumental</b>")
      result should include("Manual override")
    }
  }

  "POST instrumental/artist returns instrumental marker" in {
    postString(uri"lyrics/instrumental/artist/$songPath").map { result =>
      result should include("<b>Instrumental</b>")
      result should include("Default for artist")
    }
  }

  "POST push with unsupported URL returns error" in {
    postStringWithBody(
      uri"lyrics/push/$songPath",
      "http://www.unknown-lyrics-site.com/song123",
    ).map { result =>
      result should include("No retriever could parse host")
    }
  }
}
