package mains.fixer.new_artist

import java.awt.Color
import java.io.File

import javax.imageio.ImageIO
import mains.SwingUtils._

import scala.swing.{BoxPanel, Label, Orientation}

import common.rich.RichT._
import common.rich.path.Directory

private class GenreBox(
    val directory: Directory, orientation: Orientation.Value, fontSize: Option[Int], iconSideInPixels: Int,
) extends BoxPanel(orientation) {
  private val folderImage: File = directory.\("folder.jpg")
      .optFilter(_.exists)
      .getOrElse(directory / "folder.png")
  private val image = ImageIO.read(folderImage)
      .toSquareImageIcon(iconSideInPixels)
      .toComponent
  contents += image
  private val label = new Label(directory.name).joinOption(fontSize)(_ setFontSize _)
  contents += label

  private val originalBackground = this.background
  def reset(): Unit = this.background = originalBackground
  def enableIfFuzzyMatch(s: String): Boolean = {
    val $ = isFuzzyMatch(s)
    this.background = if ($) Color.WHITE else Color.BLACK
    $
  }
  def isFuzzyMatch(s: String): Boolean = FuzzyMatch(s, label.text)
}
