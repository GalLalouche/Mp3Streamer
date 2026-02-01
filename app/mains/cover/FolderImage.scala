package mains.cover

import java.io.File
import java.nio.file.{Files, StandardCopyOption}
import javax.swing.ImageIcon

import common.path.ref.FileRef
import common.path.ref.io.{IODirectory, IOFile}

private trait FolderImage {
  def file: FileRef
  def isLocal: Boolean
  def width: Int
  def height: Int
  def toIcon(requestedWidth: Int, requestedHeight: Int): ImageIcon

  def move(to: IODirectory): Unit = Files.move(
    file.asInstanceOf[IOFile].toPath,
    new File(to, "folder.jpg").toPath,
    StandardCopyOption.REPLACE_EXISTING,
  )
  override def toString =
    s"FolderImage(file: $file, isLocal: $isLocal, width: $width, height: $height)"
}
