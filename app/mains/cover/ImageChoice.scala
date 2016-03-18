package mains.cover
import scala.swing.event.Event

/** A choice about image selection done by the user */
private sealed abstract class ImageChoice extends Event

private case class Selected(image: FolderImage) extends ImageChoice
private case object Cancelled extends ImageChoice
private case object OpenBrowser extends ImageChoice
