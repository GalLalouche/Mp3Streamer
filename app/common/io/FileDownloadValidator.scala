package common.io

import java.io.File
import javax.inject.Inject

import better.files.FileExtensions

import common.rich.primitives.RichOption.richOption

/**
 * Validates that the file is in the music directory, and has a required extension. This ensures
 * general files from anywhere in the host computer aren't available for download.
 */
class FileDownloadValidator @Inject() (@BaseDirectory baseDir: DirectoryRef) {
  /**
   * Throws an IllegalArgumentException if file is not in [[baseDir]], or if its extension doesn't
   * match the allowed ones
   */
  // Note: this uses File and not FileRef, since the file may not exist, but at this stage we don't
  // want to leak information about which files exist and don't, if they don't pass this validation.
  def apply(file: File, allowedExtensions: Set[String]): Unit = {
    val path = file.getAbsolutePath
    require(
      baseDir.isDescendant(path),
      s"Can only download file from the music directory <${baseDir.path}>, but path was <$path>",
    )
    val extension = file.toScala.extension.getOrThrow(s"File <$file> has no extension").tail
    require(
      allowedExtensions.contains(extension),
      s"Can only download files with extension <${allowedExtensions.mkString("[", ",", "]")}>, " +
        s"but extension was <$extension>",
    )
  }
}
