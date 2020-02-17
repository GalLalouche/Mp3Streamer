package mains.cover

private[mains] sealed trait CoverException extends Exception
private[mains] object CoverException {
  class UserOpenedBrowser extends CoverException
  class UserClosedGUI extends CoverException
}
