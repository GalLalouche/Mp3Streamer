package mains

import common.Debug
import loggers.ConsoleLogger
import search.MetadataCacher

object SongParser extends Debug {
  def main(args: Array[String]) {
    timed("parsing all files", ConsoleLogger) {
      MetadataCacher.indexAll(_ => ())
    }
    sys.exit()
  }
}
