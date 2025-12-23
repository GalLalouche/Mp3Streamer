package mains.cover

private[mains] sealed trait CoverException extends Exception
private[mains] object CoverException {
  case object UserOpenedBrowser extends CoverException
  case object UserClosedGUI extends CoverException
}
