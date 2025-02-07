package controllers

import models.{AlbumDir, ModelJsonable}

import common.rich.func.MoreTraverseInstances._
import scalaz.Scalaz.ToFunctorOps
import scalaz.std.option.optionInstance
import scalaz.syntax.traverse.ToTraverseOps

import common.json.{JsonableOverrider, OJsonable}
import common.json.RichJson._

/**
 * Adds classical music information, as well as encoding dir path. This is a class and not a
 * singleton object to decouple formatters from controller code.
 */
class ControllerAlbumDirJsonifier {
  implicit val albumDirJsonable: OJsonable[AlbumDir] =
    JsonableOverrider.oJsonify[AlbumDir]((a, original) =>
      original
        .append("discNumbers" -> ControllerAlbumDirJsonifier.discNumbers(a))
        .append("composer" -> a.composer)
        .append("conductor" -> a.conductor)
        .append("opus" -> a.opus)
        .append("orchestra" -> a.orchestra)
        .append("performanceYear" -> a.performanceYear)
        .append("dir" -> Some(PlayUrlEncoder(a.dir))),
    )(ModelJsonable.AlbumDirJsonifier)
}

private object ControllerAlbumDirJsonifier {
  // If not all songs have a disc number, returns None (i.e., ignores albums with bonus disc only).
  private def discNumbers(a: AlbumDir): Option[Seq[String]] =
    a.songs
      .traverse(s => s.discNumber.strengthR(s.trackNumber))
      // Sort disc numbers by track order.
      .map(_.sortBy(_._2).map(_._1).distinct)
}
