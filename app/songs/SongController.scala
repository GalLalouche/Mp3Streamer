package songs

import javax.inject.Inject

import controllers.{PlayActionConverter, PlayControllerUtils}
import play.api.mvc._
import songs.SongFormatter.ShouldEncodeMp3Reader

/** Handles fetch requests of JSON information. */
class SongController @Inject() (
    $ : SongFormatter,
    converter: PlayActionConverter,
) extends InjectedController {
  private def run(r: ShouldEncodeMp3Reader): Action[AnyContent] =
    converter.parse(PlayControllerUtils.shouldEncodeMp3)(r.run)

  def randomSong = run($.randomSong())
  // For debugging
  def randomMp3Song = run($.randomMp3Song())
  def randomFlacSong = run($.randomFlacSong())

  def album(path: String) = run($.album(path))
  def discNumber(path: String, requestedDiscNumber: String) = run(
    $.discNumber(path, requestedDiscNumber),
  )

  def song(path: String) = run($.song(path))
  def nextSong(path: String) = run($.nextSong(path))
}
