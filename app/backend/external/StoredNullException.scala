package backend.external

private class StoredNullException(val shouldReport: Boolean, msg: String) extends Exception(msg)

private object StoredNullException {
  def unapply(e: StoredNullException): Option[Boolean] = Some(e.shouldReport)
}
