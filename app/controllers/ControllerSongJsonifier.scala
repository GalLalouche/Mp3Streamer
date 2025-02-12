package controllers

import javax.inject.Inject

import models.{IOSong, ModelJsonable, PosterLookup, Song}
import models.ModelJsonable.SongJsonifier
import play.api.libs.json.{JsObject, JsString}

import common.json.{JsonableOverrider, OJsonable, OJsonableOverrider}
import common.json.RichJson._
import common.rich.RichT._

/**
 * Jsonifies a song in a format that is more palatable to the client:
 *   - `file` is encoded and decoded as URL
 *   - Adds other relevant paths such as stream path and posters
 */
class ControllerSongJsonifier @Inject() (
    posterLookup: PosterLookup,
    encoder: Encoder,
    decoder: Decoder,
) {
  implicit val songJsonable: OJsonable[Song] = JsonableOverrider(new OJsonableOverrider[Song] {
    override def jsonify(s: Song, original: => JsObject) = {
      val posterPath =
        encoder(posterLookup.getCoverArt(s.asInstanceOf[IOSong]))
          // It's possible the playlist contains files which have already been deleted.
          .onlyIf(s.file.exists)
      val $ = original +
        ("file" -> JsString(encoder(s))) +
        (s.file.extension -> JsString("/stream/download/" + encoder(s)))
      $.joinOption(posterPath)((j, p) => j + ("poster" -> JsString("/posters/" + p)))
        .append("composer" -> s.composer)
        .append("conductor" -> s.conductor)
        .append("orchestra" -> s.orchestra)
        .append("opus" -> s.opus)
        .append("performanceYear" -> s.performanceYear)
    }
    override def parse(obj: JsObject, unused: => Song) =
      SongJsonifier.parse(obj + ("file" -> JsString(decoder(obj.str("file")))))
  })(ModelJsonable.SongJsonifier)
}
