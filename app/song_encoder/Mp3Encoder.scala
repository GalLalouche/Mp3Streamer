package song_encoder

import java.time.LocalDateTime
import java.util.regex.Pattern

import com.google.inject.{Inject, Singleton}

import scala.concurrent.{ExecutionContext, Future}
import scala.math.Ordering.Implicits.infixOrderingOps

import common.rich.func.kats.ToMoreApplyOps.toMoreApplyOps

import common.concurrency.actor.{Extra, SimpleTypedActor}
import common.io.RootDirectory
import common.path.ref.{DirectoryRef, FileRef}
import common.rich.RichT._
import common.rich.primitives.RichBoolean.richBoolean
import common.rich.primitives.RichString._

/** Encodes audio files to mp3. Also handles caching. */
@Singleton // Needed for unique actors
class Mp3Encoder @Inject() (
    @RootDirectory rootDirectory: DirectoryRef,
    encoder: SongEncoder,
    ec: ExecutionContext,
) extends SimpleTypedActor[FileRef, FileRef] {
  private implicit val iec: ExecutionContext = ec
  private val outputDir = rootDirectory.addSubDir("musicOutput")
  private val cleaner = Extra(s"FolderCleaner for <$outputDir>") {
    val minimumCreationTime = LocalDateTime.now.minusWeeks(1)
    outputDir.files.filter(_.lastAccessTime < minimumCreationTime).foreach(_.delete)
  }

  private val actor = SimpleTypedActor.unique("Mp3Encoder", encodeFileIfNeeded)

  private def encodeFileIfNeeded(f: FileRef): FileRef =
    f.mapIf(_.hasExtension("mp3").isFalse).to(encode(_))

  private def encode(file: FileRef): FileRef = {
    require(file.exists)
    val outputFileName = file.path.removeAll(Mp3Encoder.PathChars).toLowerCase + ".mp3"
    outputDir.files.find(_.name == outputFileName).filter(_.size > 0).getOrElse {
      outputDir.addFile(outputFileName).<|(encoder.encode(file, _))
    }
  }

  /**
   * Encode the file to an MP3 format. The file will only be created if its matching output doesn't
   * already exist.
   *
   * @return
   *   The (possibly new) mp3 file created; The file will be created in the outputDir, and will be
   *   the absolute path of the file (with no spaces) with an "mp3" extension.
   */
  override def !(m: => FileRef): Future[FileRef] = actor.!(m) <<* cleaner.!()
}

private object Mp3Encoder {
  private val PathChars = Pattern.compile("""[\s\/\\\-:]""")
}
