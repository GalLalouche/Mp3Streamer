package decoders

import common.concurrency.SimpleTypedActor
import common.io.{DirectoryRef, FileRef, FolderCleaner, RootDirectory}
import common.rich.RichT._
import javax.inject.Inject

import scala.concurrent.{ExecutionContext, Future}

import scalaz.std.FutureInstances
import scalaz.syntax.ToApplicativeOps

/** Encodes audio files files to mp3. Also handles caching. */
class Mp3Encoder @Inject()(
    @RootDirectory rootDirectory: DirectoryRef,
    encoder: Encoder,
    ec: ExecutionContext,
) extends SimpleTypedActor[FileRef, FileRef]
    with ToApplicativeOps with FutureInstances {
  private implicit val iec: ExecutionContext = ec
  private val outputDir = rootDirectory addSubDir "musicOutput"
  private val cleaner = new FolderCleaner(outputDir)
  private val actor = SimpleTypedActor.unique("Mp3Encoder", encodeFileIfNeeded)

  private def encodeFileIfNeeded(f: FileRef) = f.mapIf(_.extension.toLowerCase != "mp3").to(encode(_))

  private def encode(file: FileRef): FileRef = {
    require(file.exists)
    val outputFileName = file.path.replaceAll("""[\s\/\\\-:]""", "").toLowerCase + ".mp3"
    outputDir.files.find(_.name == outputFileName).filter(_.size > 0) getOrElse {
      val $ = outputDir.addFile(outputFileName)
      encoder.encode(file, $, Mp3)
      $
    }
  }

  /**
   * Encode the file to an mp3 format.
   * The file will only be created if its matching output doesn't already exist.
   * @return The (possibly new) mp3 file created; The file will be created in the outputDir, and will
   *         be the absolute path of the file (with no space) with an "mp3" extension.
   */
  override def !(m: => FileRef): Future[FileRef] = actor.!(m) <* cleaner.!()
}

