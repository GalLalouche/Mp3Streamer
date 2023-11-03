package mains.splitter

import net.codingwell.scalaguice.ScalaModule

private object SplitterModule extends ScalaModule {
  override def configure() =
    bind[CueSplitter].toInstance(Mp3SpltSplitter)
}
