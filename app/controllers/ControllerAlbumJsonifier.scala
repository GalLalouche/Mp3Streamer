package controllers

import javax.inject.Inject

import models.{AlbumDir, ModelJsonable}

import common.rich.func.MoreTraverseInstances._
import scalaz.std.option.optionInstance
import scalaz.syntax.traverse.ToTraverseOps

import common.json.{JsonableOverrider, OJsonable}
import common.json.RichJson._

class ControllerAlbumJsonifier @Inject() {
  implicit val albumJsonable: OJsonable[AlbumDir] =
    JsonableOverrider.oJsonify[AlbumDir]((a, original) =>
      original
        .append("discNumbers" -> ControllerAlbumJsonifier.discNumbers(a))
        .append("composer" -> a.composer)
        .append("conductor" -> a.conductor)
        .append("opus" -> a.opus)
        .append("orchestra" -> a.orchestra)
        .append("performanceYear" -> a.performanceYear)
        .append("dir" -> Some(PlayUrlEncoder(a.dir))),
    )(ModelJsonable.AlbumDirJsonifier)
}

private object ControllerAlbumJsonifier {
  // If not all songs have a disc number, returns None (i.e., ignores albums with bonus disc only).
  private def discNumbers(a: AlbumDir): Option[Seq[String]] =
    a.songs
      .traverse(s => s.discNumber.map(_ -> s.trackNumber))
      // Sort disc numbers by track order.
      .map(_.sortBy(_._2).map(_._1).distinct)
}
