package mains

import common.Debug
import controllers.RealLocations
import search.MetadataCacher

object SongParser extends Debug {
  def main(args: Array[String]) {
    timed("parsing all files") {
      MetadataCacher.indexAll(RealLocations)
    }
    sys.exit()
  }
}
