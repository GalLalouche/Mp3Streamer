package mains.cover

import javax.imageio.ImageIO

import backend.Url
import common.io.{FileRef, IOFile}

private sealed trait ImageSource

private case class UrlSource(url: Url, width: Int, height: Int) extends ImageSource

private case class LocalSource(file: FileRef) extends ImageSource
