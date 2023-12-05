package backend.lucky

import javax.inject.Inject
import scala.concurrent.Future

import io.lemonlabs.uri.Url

private class LuckyFormatter @Inject() ($ : LuckyModel) {
  def search(query: String): Future[String] = $.search(query)
}
