package backend.storage

import scala.concurrent.Future

/** A SQL oriented functionally for handling table creation and destruction. */
trait StorageUtils {
  /** Returns true if a table was created, i.e., did not exist before; false otherwise. */
  def createTable(): Future[Boolean]
  /** Returns true if a table was deleted, i.e., exists; false otherwise. */
  def clearTable(): Future[Boolean]
  /** Returns true if a table was dropped, i.e., did exist before; false otherwise. */
  def dropTable(): Future[Boolean]
  def doesTableExist: Future[Boolean]
}
