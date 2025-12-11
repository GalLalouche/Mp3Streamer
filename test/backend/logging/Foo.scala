package backend.logging

class Foo {
  def info(): Unit =
    scribe.info("Foo info")
}
