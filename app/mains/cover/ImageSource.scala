package mains.cover
import backend.Url
import common.io.FileRef

private sealed trait ImageSource
private case class UrlSource(url: Url) extends ImageSource
private case class LocalSource(file: FileRef) extends ImageSource
