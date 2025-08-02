package mains.cleaner

import backend.module.StandaloneModule
import backend.recon.{Artist, ReconcilableFactory}
import backend.recon.Reconcilable.SongExtractor
import com.google.inject.{Guice, Inject}
import me.tongfei.progressbar.ProgressBar
import models.IOSongTagParser
import musicfinder.{ArtistDirsIndex, IOMusicFinder}
import net.codingwell.scalaguice.InjectorExtensions.ScalaInjector

import scala.collection.mutable.ArrayBuffer

import common.io.DirectoryRef

private class FindArtistWithMismatchedFolderName @Inject() (
    mf: IOMusicFinder,
    rf: ReconcilableFactory,
    artistDirsIndex: ArtistDirsIndex,
) {
  def go: (ProgressBar, Seq[(Artist, DirectoryRef)]) = {
    val pb = new ProgressBar("Traversing directories", mf.artistDirs.length)
    (
      pb,
      for {
        artistDir <- mf.artistDirs
        _ = pb.step()
        artist = ???
        // artistDirsIndex
        //  .forDir(artistDir)
        //  .asInstanceOf[SingleArtist]
        //  .artist
        mismatchedArtist <-
          artistDir.deepDirs
            .+:(artistDir)
            .view
            .map(mf.getSongFilesInDir(_))
            .filter(_.nonEmpty)
            .map(IOSongTagParser apply _.head.file)
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

    val buffer = new ArrayBuffer[String]()
    for ((a, d) <- dirs) {
      val s = s""""${d.name}" -> "${a.name}","""
      buffer += s
      println(s)
    }
    println("Summary:")
    println(buffer.mkString("\n"))
    pb.close()
  }
}
