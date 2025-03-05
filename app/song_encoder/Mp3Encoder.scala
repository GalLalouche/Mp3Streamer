package song_encoder

import java.util.regex.Pattern

import com.google.inject.{Inject, Singleton}

import scala.concurrent.{ExecutionContext, Future}

import common.rich.func.BetterFutureInstances._
import scalaz.syntax.apply.ToApplyOps

import common.concurrency.SimpleTypedActor
import common.io.{DirectoryRef, FileRef, FolderCleaner, RootDirectory}
import common.rich.RichT._
import common.rich.primitives.RichBoolean.richBoolean
import common.rich.primitives.RichString._

/** Encodes audio files to mp3. Also handles caching. */
@Singleton // Needed for unique actor
class Mp3Encoder @Inject() (
    @RootDirectory rootDirectory: DirectoryRef,
    encoder: SongEncoder,
    ec: ExecutionContext,
) extends SimpleTypedActor[FileRef, FileRef] {
  private implicit val iec: ExecutionContext = ec
  private val outputDir = rootDirectory.addSubDir("musicOutput")
  private val cleaner = new FolderCleaner(outputDir)
  private val actor = SimpleTypedActor.unique("Mp3Encoder", encodeFileIfNeeded)

  private def encodeFileIfNeeded(f: FileRef) =
    f.mapIf(_.extension.equalsIgnoreCase("mp3").isFalse).to(encode(_))

  private def encode(file: FileRef): FileRef = {
    require(file.exists)
    val outputFileName = file.path.removeAll(Mp3Encoder.PathChars).toLowerCase + ".mp3"
    outputDir.files.find(_.name == outputFileName).filter(_.size > 0).getOrElse {
      val $ = outputDir.addFile(outputFileName)
      encoder.encode(file, $, Mp3)
      $
    }
  }

  /**
   * Encode the file to an mp3 format. The file will only be created if its matching output doesn't
   * already exist.
   *
   * @return
   *   The (possibly new) mp3 file created; The file will be created in the outputDir, and will be
   *   the absolute path of the file (with no spaces) with an "mp3" extension.
   */
  override def !(m: => FileRef): Future[FileRef] = actor.!(m) <* cleaner.clean()
}

private object Mp3Encoder {
  private val PathChars = Pattern.compile("""[\s\/\\\-:]""")
}
