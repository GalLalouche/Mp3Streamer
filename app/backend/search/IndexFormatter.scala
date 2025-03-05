package backend.search

import com.google.inject.Inject

import scala.concurrent.Future

class IndexFormatter @Inject() (indexer: Indexer) {
  def index(): Future[_] = indexer.index()
}
