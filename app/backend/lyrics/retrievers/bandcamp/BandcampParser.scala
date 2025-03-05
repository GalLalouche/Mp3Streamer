package backend.lyrics.retrievers.bandcamp

import backend.lyrics.retrievers.{PassiveParser, SingleHostParsingHelper}
import com.google.inject.Inject

private[retrievers] class BandcampParser @Inject() (helper: SingleHostParsingHelper)
    extends PassiveParser {
  override val parse = helper(SingleSongParser)
  override val doesUrlMatchHost = Utils.doesUrlMatchHost
}
