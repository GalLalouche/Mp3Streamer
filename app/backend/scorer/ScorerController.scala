package backend.scorer

import javax.inject.Inject

import controllers.{PlayActionConverter, UrlDecodeUtils}
import play.api.mvc.InjectedController

class ScorerController @Inject() (
    $ : ScorerFormatter,
    converter: PlayActionConverter,
    decoder: UrlDecodeUtils,
) extends InjectedController {
  def getScore(filePath: String) = converter.ok($.getScore(filePath))
  def updateSongScore(filePath: String, score: String) = {
    val str = decoder.decode(filePath)
    scribe.info(s"Updating song score to <$score> for <$str>")
    converter.noContent($.updateSongScore(str, score))
  }
  def updateAlbumScore(filePath: String, score: String) = {
    val str = decoder.decode(filePath)
    scribe.info(s"Updating album score to <$score> for <$str>")
    converter.noContent($.updateAlbumScore(str, score))
  }
  def updateArtistScore(filePath: String, score: String) = {
    val str = decoder.decode(filePath)
    scribe.info(s"Updating artist score to <$score> for <$str>")
    converter.noContent($.updateArtistScore(str, score))
  }
}
