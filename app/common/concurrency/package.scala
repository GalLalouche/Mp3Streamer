package common

import scala.concurrent.Future

package object concurrency {
  type FutureIterant[A] = Iterant[Future, A]
}
