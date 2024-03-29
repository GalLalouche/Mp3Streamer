package mains.cleaner

import javax.inject.Inject

import backend.module.StandaloneModule
import backend.recon.{Artist, ReconcilableFactory}
import backend.recon.Reconcilable.SongExtractor
import com.google.inject.Guice
import models.{IOMusicFinder, SongTagParser}
import net.codingwell.scalaguice.InjectorExtensions.ScalaInjector

import common.io.DirectoryRef

private class FindArtistWithMismatchedFolderName @Inject() (
    mf: IOMusicFinder,
    rf: ReconcilableFactory,
) {
  def go: Seq[(Artist, DirectoryRef)] = for {
    artistDir <- mf.artistDirs
    artist = SongTagParser(
      artistDir.deepDirs
        .+:(artistDir)
        .view
        .flatMap(mf.getSongFilesInDir)
        .ensuring(_.nonEmpty, s"$artistDir has no files?")
        .head
        .file,
    ).artist
    if artist != rf.toArtist(artistDir)
  } yield (artist, artistDir)
}

private object FindArtistWithMismatchedFolderName {
  def main(args: Array[String]): Unit =
    println(
      Guice
        .createInjector(StandaloneModule)
        .instance[FindArtistWithMismatchedFolderName]
        .go
        .map { case (a, d) => s""""${d.name}" -> "${a.name}",""" }
        .toVector
        .mkString("\n"),
    )
}
