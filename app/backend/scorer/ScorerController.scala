package backend.scorer

import controllers.{PlayActionConverter, UrlDecodeUtils}
import javax.inject.Inject
import play.api.mvc.InjectedController

class ScorerController @Inject()($: ScorerFormatter, converter: PlayActionConverter, decoder: UrlDecodeUtils)
    extends InjectedController {
  def getScore(filePath: String) = converter.ok($.getScore(filePath))
  def updateSongScore(filePath: String, score: String) =
    converter.noContent($.updateSongScore(decoder.decode(filePath), score))
  def updateAlbumScore(filePath: String, score: String) =
    converter.noContent($.updateAlbumScore(decoder.decode(filePath), score))
  def updateArtistScore(filePath: String, score: String) =
    converter.noContent($.updateArtistScore(decoder.decode(filePath), score))
}
