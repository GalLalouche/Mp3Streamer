package backend.lyrics.retrievers.bandcamp

import javax.inject.Inject

import backend.lyrics.retrievers.{PassiveParser, SingleHostParsingHelper}

private[retrievers] class BandcampParser @Inject() (helper: SingleHostParsingHelper)
    extends PassiveParser {
  override val parse = helper(SingleSongParser)
  override val doesUrlMatchHost = Utils.doesUrlMatchHost
}
