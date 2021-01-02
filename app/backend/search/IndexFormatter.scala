package backend.search

import javax.inject.Inject

import scala.concurrent.Future

private class IndexFormatter @Inject()(indexer: Indexer) {
  def index(): Future[_] = indexer.index()
}
