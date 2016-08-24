package backend

import scala.concurrent.Future

package object storage {
  type Retriever[Key, Value] = Key => Future[Value]
}
