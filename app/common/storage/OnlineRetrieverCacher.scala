package common.storage

import scala.concurrent.{ExecutionContext, Future}

/**
 * Tries to first retrieve information from a local repository.
 * If it fails, it will try to use an online API, and save the result persistently.
 */
class OnlineRetrieverCacher[Key, Value](localStorage: LocalStorage[Key, Value], onlineRetriever: Key => Future[Value])
    (implicit ec: ExecutionContext) {
  def get(k: Key): Future[Value] = localStorage.load(k)
      .recoverWith {case e =>
        val f = onlineRetriever(k)
        f.foreach(localStorage.store(k, _))
        f
      }
}
