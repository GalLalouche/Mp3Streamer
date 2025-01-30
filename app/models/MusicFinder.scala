package models

import backend.recon.{Artist => BRArtist}
import com.google.common.collect.BiMap
import models.MusicFinder.DirectoryName

import common.ds.Types.ViewSeq
import common.io.{DirectoryRef, FileRef, RefSystem}
import common.rich.RichT.richT

trait MusicFinder { self =>
  type S <: RefSystem { type S = self.S }
  def baseDir: S#D
  def extensions: Set[String]
  /** Known file extensions which aren't for various reasons, e.g., monkey 🙉. */
  def unsupportedExtensions: Set[String]

  /**
   * Dirs with an extra level of sub-genre nesting, e.g., metal which has death and black metal
   * inside it.
   */
  protected def genresWithSubGenres: Seq[String]
  /** Dirs which host artists/albums at the top level, e.g., musicals. */
  def flatGenres: Seq[String]
  private def getDir(name: String) = baseDir.getDir(name).get
  private def allGenres = genresWithSubGenres ++ flatGenres
  def genreDirsWithSubGenres: Seq[S#D] = genresWithSubGenres.map(getDir)
  def genreDirs: Seq[S#D] = allGenres.sorted.map(getDir)
  def artistDirs: Seq[S#D] = {
    def getDirs(xs: Seq[String]): Seq[S#D] = xs.view.map(getDir).flatMap(_.dirs)
    getDirs(genresWithSubGenres).flatMap(_.dirs) ++ getDirs(flatGenres)
  }
  // TODO this naming ambiguity has got to go!
  def findArtistDir(artist: BRArtist): Option[S#D] =
    artistDirs.find(dir => artist == dirNameToArtist(dir.name))

  def dirNameToArtist(name: DirectoryName): BRArtist =
    BRArtist(name).optionOrKeep(invalidDirectoryNames.get(_).opt)
  // Some artists have invalid directory characters in their name, so their directory won't match
  // the artist name. As a stupid hack, just aggregate them below.
  protected def invalidDirectoryNames: BiMap[DirectoryName, BRArtist]
  def albumDirs: DirView = albumDirs(genreDirs)
  def albumDirs(startingFrom: Seq[S#D]): DirView = startingFrom.view
    .flatMap(_.deepDirs)
    // Because some albums have, e.g., cover subdirectories
    .filter(_.files.exists(f => extensions.contains(f.extension)))
  def getSongFiles: ViewSeq[S#F] = albumDirs.flatMap(getSongFilesInDir)
  def getSongFilesInDir(d: DirectoryRef): Seq[S#F] =
    d.asInstanceOf[S#D].files.filter(f => extensions.contains(f.extension))
  def parseSong(f: FileRef): Song { type F = S#F }
  def getSongsInDir(d: DirectoryRef): ViewSeq[Song { type F = S#F }] =
    getSongFilesInDir(d).view.map(parseSong)
  def getOptionalSongsInDir(d: DirectoryRef): ViewSeq[OptionalSong]

  final type DirView = ViewSeq[S#D]
}

object MusicFinder {
  type DirectoryName = String
}
