package mains.cover

import scala.swing.event.Event

/** A choice about image selection done by the user */
private sealed abstract class ImageChoice extends Event

private object ImageChoice {
  case class Selected(image: FolderImage) extends ImageChoice
  case object Cancelled extends ImageChoice
  case object OpenBrowser extends ImageChoice
  case object ImageServerTimeout extends ImageChoice
}
