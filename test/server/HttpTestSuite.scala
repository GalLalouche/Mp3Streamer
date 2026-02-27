package server

import com.google.inject.Module
import org.scalatest.{AsyncTestSuite, Suite}

import scala.collection.immutable

class HttpTestSuite(serverModule: Module) extends AsyncTestSuite {
  override def nestedSuites: immutable.IndexedSeq[Suite] = Vector(
    new ApplicationTest(serverModule),
    new AssetTest(serverModule),
    new ExternalTest(serverModule),
    new IndexTest(serverModule),
    new LastAlbumsServerTest(serverModule),
    new LuckyTest(serverModule),
    new LyricsTest(serverModule),
    new NewAlbumTest(serverModule),
    new PlaylistTest(serverModule),
    new PosterTest(serverModule),
    new RecentTest(serverModule),
    new ScoreTest(serverModule),
    new SearchTest(serverModule),
    new SongTest(serverModule),
    new StreamTest(serverModule),
  )
}
