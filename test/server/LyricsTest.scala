package server

import backend.lyrics.retrievers.InstrumentalArtistStorage
import backend.recon.ArtistReconStorage
import com.google.inject.Module
import net.codingwell.scalaguice.InjectorExtensions.ScalaInjector
import sttp.client3.UriContext

import scala.concurrent.Future

import common.rich.func.kats.ToMoreApplyOps.toMoreApplyOps

import common.test.BeforeAndAfterEachAsync

private class LyricsTest(serverModule: Module)
    extends HttpServerSpecs(serverModule)
    with BeforeAndAfterEachAsync {

  // Must use a relative path because Http4sUtils.decodePath strips the leading '/'.
  private val songPath = relativePath(getResourceFile("/models/song.mp3"))

  private val artistReconStorage = injector.instance[ArtistReconStorage]
  private val instrumentalArtistStorage = injector.instance[InstrumentalArtistStorage]

  override def beforeEach(): Future[_] =
    artistReconStorage.utils.clearOrCreateTable() *>>
      instrumentalArtistStorage.utils.clearOrCreateTable()

  "GET returns failure message when no lyrics are found" in {
    getString(uri"lyrics/$songPath") shouldEventuallyReturn "Failed to get lyrics :("
  }

  private val instrumentalSongResponse =
    "<img src='assets/images/TrebleClef.png' width='30' height='68' /><b>Instrumental</b><br><br>Source: Manual override"
  "POST instrumental/song marks song as instrumental" in {
    postString(
      uri"lyrics/instrumental/song/$songPath",
    ) shouldEventuallyReturn instrumentalSongResponse
  }

  "GET returns cached instrumental after setting song as instrumental" in {
    postString(uri"lyrics/instrumental/song/$songPath") *>>
      getString(uri"lyrics/$songPath") shouldEventuallyReturn instrumentalSongResponse
  }

  "POST instrumental/artist returns instrumental marker" in {
    postString(uri"lyrics/instrumental/artist/$songPath") shouldEventuallyReturn
      "<img src='assets/images/TrebleClef.png' width='30' height='68' /><b>Instrumental</b><br><br>Source: Default for artist"
  }

  "POST push with unsupported URL returns error" in {
    postStringWithBody(
      uri"lyrics/push/$songPath",
      "http://www.unknown-lyrics-site.com/song123",
    ) shouldEventuallyReturn "No retriever could parse host &lt;www.unknown-lyrics-site.com&gt;"
  }
}
