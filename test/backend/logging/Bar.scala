package backend.logging

class Bar {
  def debug(): Unit =
    scribe.debug("Bar debug")
}
