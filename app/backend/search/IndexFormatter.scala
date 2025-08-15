package backend.search

import com.google.inject.Inject

import scala.concurrent.Future

class IndexFormatter @Inject() (indexer: Indexer) {
  def index(forceRefresh: Boolean): Future[_] = indexer.index(forceRefresh: Boolean)
}
