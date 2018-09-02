package backend.search

import backend.logging.Logger
import com.google.inject.{Provider, Singleton}
import common.concurrency.Extra
import javax.inject.Inject
import models.{Album, Artist, Song}

@Singleton
class SearchState @Inject()(compositeIndexProvider: Provider[CompositeIndex], logger: Logger) extends Extra {
  private var index: CompositeIndex = compositeIndexProvider.get()

  override def apply(): Unit = {
    index = compositeIndexProvider.get()
    logger info "Search state has been updated"
  }

  def search(terms: Seq[String]): (Seq[Song], Seq[Album], Seq[Artist]) = index search terms
}
