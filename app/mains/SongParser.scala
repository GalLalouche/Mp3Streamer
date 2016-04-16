package mains

import common.Debug
import search.MetadataCacher

object SongParser extends Debug {
  def main(args: Array[String]) {
    timed("parsing all files") {
      MetadataCacher.indexAll().subscribe()
    }
    sys.exit()
  }
}
