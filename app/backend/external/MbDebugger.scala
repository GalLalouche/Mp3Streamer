package backend.external

import backend.configs.{Configuration, StandaloneConfig}
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
    implicit val c: Configuration = StandaloneConfig
    implicit val ec: ExecutionContext = c.injector.instance[ExecutionContext]
    val $ = new MbExternalLinksProvider()
    val s = fromDir("""D:\\Media\\Music\\Rock\\Classical Prog\\The Moody Blues\\1969 On the Threshold of a Dream""")

    println($(s).artistLinks.get)
    println($(s).albumLinks.get)
  }
}
