package mains.cover

import java.awt.image.BufferedImage
import javax.imageio.ImageIO

import io.lemonlabs.uri.Url

import common.path.ref.FileRef
import common.path.ref.io.IOFile

private sealed trait ImageSource {
  def width: Int
  def height: Int
  def isSquare: Boolean = width == height
}

private object ImageSource {
  case class UrlSource(url: Url, override val width: Int, override val height: Int)
      extends ImageSource
  case class LocalSource(file: FileRef) extends ImageSource {
    lazy val image: BufferedImage = ImageSource.toImage(file)
    override lazy val width: Int = image.getWidth
    override lazy val height: Int = image.getHeight
  }
  def toImage(f: FileRef): BufferedImage = ImageIO.read(f.asInstanceOf[IOFile])
}
