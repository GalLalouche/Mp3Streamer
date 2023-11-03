package backend.lyrics.retrievers.bandcamp

import backend.lyrics.retrievers.{PassiveParser, SingleHostParsingHelper}
import javax.inject.Inject

private[retrievers] class BandcampParser @Inject() (helper: SingleHostParsingHelper)
    extends PassiveParser {
  override val parse = helper(SingleSongParser)
  override val doesUrlMatchHost = Utils.doesUrlMatchHost
}
