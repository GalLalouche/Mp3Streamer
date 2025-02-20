package server

import com.google.inject.Module
import org.scalatest.{AsyncTestSuite, Suite}

import scala.collection.immutable

class HttpTestSuite(serverModule: Module) extends AsyncTestSuite {
  override def nestedSuites: immutable.IndexedSeq[Suite] = Vector(
    new LuckyTest(serverModule),
    new PlaylistTest(serverModule),
    new PosterTest(serverModule),
  )
}
