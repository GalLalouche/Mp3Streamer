package mains.fixer

import backend.FutureOption
import backend.recon.Artist
import backend.recon.Reconcilable.SongExtractor
import better.files.File.CopyOptions
import com.google.inject.{Guice, Inject}
import mains.{IOUtils, MainsModule}
import mains.cover.{CoverException, DownloadCover}
import mains.fixer.FolderFixer.{Overwrite, TempLarge}
import models.ArtistName
import musicfinder.{ArtistDirsIndex, ArtistNameNormalizer, SongDirectoryParser}
import net.codingwell.scalaguice.InjectorExtensions._

import scala.concurrent.{ExecutionContext, Future}

import cats.data.OptionT
import common.rich.func.kats.RichOptionT.richOptionT

import common.TimedLogger
import common.io.{InternetTalker, IODirectory}
import common.rich.RichFuture._
import common.rich.RichT._
import common.rich.path.{Directory, RichFileUtils}
import common.rich.primitives.RichBoolean.richBoolean
import common.rich.primitives.RichOption.richOption

private[mains] class FolderFixer @Inject() private (
    fixLabels: FixLabels,
    songDirectoryParser: SongDirectoryParser,
    artistDirsIndex: ArtistDirsIndex,
    artistNameNormalizer: ArtistNameNormalizer,
    it: InternetTalker,
    foobarGain: FoobarGain,
    downloader: DownloadCover,
    stringFixer: StringFixer,
    newArtistFolderCreator: NewArtistFolderCreator,
    ec: ExecutionContext,
    timedLogger: TimedLogger,
) {
  private implicit val iec: ExecutionContext = ec

  def run(folder: Directory): Unit = {
    val (artist, destination) = findArtistDirectory(folder)
    val folderImage = downloadCover(folder)
    println("fixing directory")
    val fixedDirectory = Future(fixLabels.fix(cloneDir(folder)))
    moveDirectory(artist.name, destination.map(_.dir), folderImage, fixedDirectory)
      .map(finish(folder, _))
      .get
  }

  /**
   * Like the above, but attempts to replace an existing folder. Will fail if an existing folder
   * will not exist. In addition, will try to reuse the existing folder image instead of downloading
   * a new one.
   */
  def replace(folder: Directory): Unit = {
    val (artist, destination) = findArtistDirectory(folder)
    replaceDirectory(artist.name, destination, Future(fixLabels.fix(cloneDir(folder))))
      .map(finish(folder, _))
      .get
  }

  private def moveDirectory(
      artist: ArtistName,
      destination: FutureOption[Directory],
      folderImage: Future[Directory => Unit],
      fixedDirectory: Future[FixedDirectory],
  ): Future[Directory] = for {
    destinationParent <-
      destination ||||
        newArtistFolderCreator
          .selectGenreDirAndPopupBrowser(artist)
          .map(_.addSubDir(stringFixer(artistNameNormalizer(artist))))
    moved <- fixedDirectory.map(d => timedLogger("Moving folder")(d.move(destinationParent)))
    folderImageMover <- folderImage
  } yield moved <| folderImageMover

  private def replaceDirectory(
      artist: String,
      destination: FutureOption[IODirectory],
      fixedDirectory: Future[FixedDirectory],
  ): Future[Directory] = for {
    artistDir <- destination.getOrThrow(s"Could not find artist directory for <$artist>")
    _ = println(s"Found artist dir: <${artistDir.file.getAbsolutePath}>")
    fixedDir <- fixedDirectory
  } yield {
    val oldDir =
      artistDir
        .getDir(fixedDir.name)
        .getOrThrow(s"Could not find existing folder with name <${fixedDir.name}>")

    println("Copying folder.jpg")
    val folderImage =
      oldDir.getFile("folder.jpg").getOrThrow(s"Could not find folder.jpg in <$oldDir>")
    folderImage.better.copyToDirectory(fixedDir.dir.better)(copyOptions = Overwrite)

    moveFolderToTemp(RichFileUtils.rename(oldDir.dir, oldDir.dir.name + " (OLD)"))
    println("Moving fixed directory to artist directory")
    fixedDir.move(artistDir.dir)
  }

  private def downloadCover(newPath: Directory): Future[Directory => Unit] =
    downloader(newPath).recover {
      case _: CoverException => println("Auto downloading picture aborted").const
      case e: RuntimeException =>
        e.printStackTrace()
        ().const
    }

  private def moveFolderToTemp(folder: Directory): Unit = {
    val target = System.getenv(TempLarge)
    require(target != null, s"Missing environment variable $TempLarge")
    println(s"Moving folder <$folder> to <$target>")
    RichFileUtils.move(folder, Directory(target))
  }

  private def findArtistDirectory(folder: Directory): (Artist, FutureOption[IODirectory]) = {
    val artist = songDirectoryParser(IODirectory(folder)).next().artist
    (artist, OptionT(Future(artistDirsIndex.forArtist(artist).map(_.asInstanceOf[IODirectory]))))
  }

  private def cloneDir(dir: Directory): Directory = {
    val newName = dir.name + "_clone"
    val newDir = dir.parent.addSubDir(newName)
    newDir.deleteAll() // delete previous directory if it exists

    timedLogger("Copying directory")(dir.better.copyTo(newDir.better, overwrite = true))
    newDir
  }

  private def finish(sourceDirectory: Directory, newDirectory: Directory): Unit = {
    IOUtils.focus(newDirectory)
    foobarGain(newDirectory)
    if (fixLabels.verify(newDirectory).isFalse)
      throw new Exception("Failed to rename some files!")
    moveFolderToTemp(sourceDirectory)
    println("--Done!--")
  }
}

private[mains] object FolderFixer {
  def main(args: Array[String]): Unit =
    Guice
      .createInjector(MainsModule)
      .instance[FolderFixer]
      .run(Directory(IOUtils.decodeFile(args(0))))

  private val TempLarge = "TEMP_LARGE"
  private val Overwrite = CopyOptions(overwrite = true)
}
