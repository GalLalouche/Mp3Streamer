package backend.external.extensions

import backend.external.{Host, MarkedLink, MarkedLinks}
import backend.recon.{Album, Artist, Reconcilable}
import common.rich.RichT._
import common.rich.primitives.RichString._

private abstract sealed class MusicBrainzExtender[R <: Reconcilable: Manifest] extends LinkExtender[R] {
  override val host = Host.MusicBrainz
  protected def extendGoogleSearch: Boolean
  private val reconcilableType = manifest.runtimeClass.getCanonicalName.takeAfterLast('.').toLowerCase
  assert(Set("artist", "album")(reconcilableType))
  protected def externalTypeIds: Map[Host, Int]

  private val dynamicExtender: DynamicExtender[R] = new DynamicExtender[R] {
    override def host: Host = MusicBrainzExtender.this.host
    override def extend(t: R, linkToModify: MarkedLink[R], otherLinks: MarkedLinks[R]) = {
      def preseededEdit(e: MarkedLink[R], i: Int): String = {
        s"edit-$reconcilableType.url.$i.text=${e.link.address}" +
            s"&edit-$reconcilableType.url.$i.link_type_id=${externalTypeIds(e.host)}"
      }
      val preseed = otherLinks
          .filter(_.isNew).toSeq
          .zipWithIndex
          .map(e => preseededEdit(e._1, e._2))
          .mkString("&")
      val editUrl = "edit" + (if (preseed.isEmpty) "" else "?" + preseed)
      append(linkToModify, "edit" -> editUrl)
          .mapIf(extendGoogleSearch)
          .to(_ ++ SearchExtension.apply(Host.MusicBrainz, t).extensions)
    }
  }

  override def extend = DynamicExtenderHelper.apply(dynamicExtender)
}

private object MusicBrainzArtistExtender extends MusicBrainzExtender[Artist] {
  override protected def extendGoogleSearch = true
  override protected val externalTypeIds = Map(
    Host.AllMusic -> 283,
    Host.Facebook -> 192, // social networking
    Host.LastFm -> 840,
    Host.RateYourMusic -> 188, // other databases
    Host.Wikipedia -> 179,
    Host.Wikidata -> 352,
  )
}

private object MusicBrainzAlbumExtender extends MusicBrainzExtender[Album] {
  override protected def extendGoogleSearch = false
  override protected val externalTypeIds = Map(
    Host.AllMusic -> 284,
    Host.Wikipedia -> 89,
    Host.Wikidata -> 353,
    Host.RateYourMusic -> 96, // other databases
  )
}
