import scala.concurrent.Future
import scalaz.OptionT

package object backend {
  type FutureOption[A] = OptionT[Future, A]
  type Retriever[A, B] = A => Future[B]
  type OptionRetriever[A, B] = A => FutureOption[B]
}
