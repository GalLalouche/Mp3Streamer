import scala.concurrent.Future

package object backend {
  type Retriever[K, V] = K => Future[V]
  type OptionRetriever[K, V] = Retriever[K, Option[V]]
}
