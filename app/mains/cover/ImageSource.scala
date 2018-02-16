package mains.cover

import java.awt.image.BufferedImage
import javax.imageio.ImageIO

import backend.Url
import common.io.{FileRef, IOFile}

private sealed trait ImageSource {
  def width: Int
  def height: Int
  def isSquare = width == height
}

private case class UrlSource(url: Url, override val width: Int, override val height: Int) extends ImageSource

private case class LocalSource(file: FileRef) extends ImageSource {
  lazy val image: BufferedImage = ImageSource toImage file
  override lazy val width: Int = image.getWidth
  override lazy val height: Int = image.getHeight
}

private object ImageSource {
  def toImage(f: FileRef): BufferedImage = ImageIO.read(f.asInstanceOf[IOFile].file)
}
