import scala.concurrent.Future

package object backend {
  type FutureOption[A] = Future[Option[A]]
  type Retriever[A, B] = A => Future[B]
  type OptionRetriever[A, B] = Retriever[A, Option[B]]
}
