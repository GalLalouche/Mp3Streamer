import scala.concurrent.Future

package object backend {
  type Retriever[K, V] = (K => Future[V])
}
