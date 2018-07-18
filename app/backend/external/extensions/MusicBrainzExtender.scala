package backend.external.extensions
import backend.external.{Host, MarkedLink, MarkedLinks}
import backend.recon.{Album, Artist, Reconcilable}

private sealed trait MusicBrainzExtender[R <: Reconcilable] extends DynamicExtender[R] {
  override val host = Host.MusicBrainz
  protected val reconcilableType: String // TODO via manifest?
  protected val externalTypeIds: Map[Host, Int]

  override protected def apply(t: R, linkToModify: MarkedLink[R], otherLinks: MarkedLinks[R]) = {
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
    append(linkToModify, "edit" -> editUrl) ++ SearchExtension.apply(Host.MusicBrainz, t).extensions.toList
  }
}

private object MusicBrainzAlbumExtender extends MusicBrainzExtender[Album] {
  override protected val reconcilableType = "album"
  override protected val externalTypeIds = Map(
    Host.AllMusic -> 284,
    Host.Wikipedia -> 89,
    Host.RateYourMusic -> 96, // other databases
  )
}

private object MusicBrainzArtistExtender extends MusicBrainzExtender[Artist] {
  override protected val reconcilableType = "artist"
  override protected val externalTypeIds = Map(
    Host.AllMusic -> 283,
    Host.Facebook -> 192, // social networking
    Host.LastFm -> 840,
    Host.RateYourMusic -> 188, // other databases
    Host.Wikipedia -> 179,
  )
}
