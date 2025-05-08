package backend.search

import com.google.inject.Inject

import scala.concurrent.Future

private class Indexer @Inject() (uniqifier: IndexerUniqifier) {
  def index(): Future[_] = uniqifier.go()
}
