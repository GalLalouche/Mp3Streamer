package backend.scorer

import javax.inject.Inject

import backend.logging.Logger
import controllers.{PlayActionConverter, UrlDecodeUtils}
import play.api.mvc.InjectedController

class ScorerController @Inject() (
    $ : ScorerFormatter,
    converter: PlayActionConverter,
    decoder: UrlDecodeUtils,
    logger: Logger,
) extends InjectedController {
  def getScore(filePath: String) = converter.ok($.getScore(filePath))
  def updateSongScore(filePath: String, score: String) = {
    val str = decoder.decode(filePath)
    logger.info(s"Updating song score to <$score> for <$str>")
    converter.noContent($.updateSongScore(str, score))
  }
  def updateAlbumScore(filePath: String, score: String) = {
    val str = decoder.decode(filePath)
    logger.info(s"Updating album score to <$score> for <$str>")
    converter.noContent($.updateAlbumScore(str, score))
  }
  def updateArtistScore(filePath: String, score: String) = {
    val str = decoder.decode(filePath)
    logger.info(s"Updating artist score to <$score> for <$str>")
    converter.noContent($.updateArtistScore(str, score))
  }
}
