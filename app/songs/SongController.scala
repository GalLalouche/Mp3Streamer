package songs

import javax.inject.Inject

import controllers.{PlayActionConverter, PlayControllerUtils, UrlDecodeUtils}
import play.api.mvc._
import songs.SongFormatter.ShouldEncodeMp3Reader

/** Handles fetch requests of JSON information. */
class SongController @Inject() (
    $ : SongFormatter,
    converter: PlayActionConverter,
    decoder: UrlDecodeUtils,
) extends InjectedController {
  private def run(r: ShouldEncodeMp3Reader): Action[AnyContent] =
    converter.parse(PlayControllerUtils.shouldEncodeMp3)(r.run)

  def randomSong = run($.randomSong())
  // For debugging
  def randomMp3Song = run($.randomMp3Song())
  def randomFlacSong = run($.randomFlacSong())

  def album(path: String) = run($.album(decoder.decode(path)))
  def discNumber(path: String, requestedDiscNumber: String) = run(
    $.discNumber(decoder.decode(path), requestedDiscNumber),
  )

  def song(path: String) = run($.song(decoder.decode(path)))
  def nextSong(path: String) = run($.nextSong(decoder.decode(path)))
}
