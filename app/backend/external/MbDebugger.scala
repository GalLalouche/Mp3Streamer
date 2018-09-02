package backend.external

import backend.module.StandaloneModule
import com.google.inject.Guice
import common.rich.RichFuture._
import common.rich.RichT._
import common.rich.path.Directory
import common.rich.path.RichFile._
import models.Song
import net.codingwell.scalaguice.InjectorExtensions._

import scala.concurrent.ExecutionContext

private object MbDebugger {
  private def fromDir(path: String): Song =
    Directory(path).files.filter(f => Set("mp3", "flac").contains(f.extension)).head |> Song.apply

  def main(args: Array[String]): Unit = {
    val injector = Guice createInjector StandaloneModule
    implicit val ec: ExecutionContext = injector.instance[ExecutionContext]
    val $ = injector.instance[MbExternalLinksProvider]
    val s = fromDir("""D:\\Media\\Music\\Rock\\Classical Prog\\The Moody Blues\\1969 On the Threshold of a Dream""")

    println($(s).artistLinks.get)
    println($(s).albumLinks.get)
  }
}
