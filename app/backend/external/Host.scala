package backend.external

import backend.Url
import common.rich.collections.RichTraversableOnce._
import common.rich.primitives.RichBoolean._

case class Host(name: String, url: Url) {
  import Host._
  def canonize: Host = hostsByName.getOrElse(name.toLowerCase.replaceAll("\\*$", ""), defaultFor(url))
}
object Host {
  private lazy val hosts: Traversable[Host] = { // extract all hosts by reflection
    import scala.reflect.runtime.{universe => u}
    u.typeOf[Host.type]
        .decls
        .flatMap(e => e.isModule.ifTrue(e.asModule))
        .map(e => u.runtimeMirror(getClass.getClassLoader).reflectModule(e).instance.asInstanceOf[Host])
  }
  private lazy val hostsByUrl = hosts.mapBy(_.url)
  private lazy val hostsByName = hosts.mapBy(_.name.toLowerCase)
  object AllMusic extends Host("AllMusic", Url("www.allmusic.com"))
  object LastFm extends Host("LastFm", Url("www.last.fm"))
  object MetalArchives extends Host("MetalArchives", Url("www.metal-archives.com"))
  object MusicBrainz extends Host("MusicBrainz", Url("musicbrainz.org"))
  object Wikipedia extends Host("Wikipedia", Url("en.wikipedia.org"))

  def fromUrl(url: Url): Option[Host] = hostsByUrl get url.host
  def defaultFor(url: Url): Host = {
    val extractedHostName = url.address
        .toLowerCase
        .replaceAll("^https?://", "")
        .replaceAll("^www\\.", "")
        .takeWhile(_ != '.')
    Host(extractedHostName, url.host)
  }
}

