package mains.cleaner

import javax.inject.Inject

import backend.module.StandaloneModule
import backend.recon.{Artist, ReconcilableFactory}
import backend.recon.Reconcilable.SongExtractor
import com.google.inject.Guice
import me.tongfei.progressbar.ProgressBar
import models.SongTagParser
import net.codingwell.scalaguice.InjectorExtensions.ScalaInjector

import common.io.DirectoryRef
import musicfinder.IOMusicFinder

private class FindArtistWithMismatchedFolderName @Inject() (
    mf: IOMusicFinder,
    rf: ReconcilableFactory,
) {
  def go: (ProgressBar, Seq[(Artist, DirectoryRef)]) = {
    val pb = new ProgressBar("Traversing directories", mf.artistDirs.length)
    (
      pb,
      for {
        artistDir <- mf.artistDirs
        _ = pb.step()
        artist = rf.toArtist(artistDir)
        mismatchedArtist <-
          artistDir.deepDirs
            .+:(artistDir)
            .view
            .map(mf.getSongFilesInDir(_))
            .filter(_.nonEmpty)
            .map(SongTagParser apply _.head.file)
            .map(_.artist)
            .find(_ != artist)
      } yield (mismatchedArtist, artistDir),
    )
  }
}

private object FindArtistWithMismatchedFolderName {
  def main(args: Array[String]): Unit = {
    val (pb, dirs) =
      Guice
        .createInjector(StandaloneModule)
        .instance[FindArtistWithMismatchedFolderName]
        .go
    println(
      dirs
        .map { case (a, d) => s""""${d.name}" -> "${a.name}",""" }
        .toVector
        .mkString("\n"),
    )
    pb.close()
  }
}
