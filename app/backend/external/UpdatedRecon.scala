package backend.external

import backend.recon.ReconID

private sealed trait UpdatedRecon
import common.rich.primitives.RichBoolean._

private object UpdatedRecon {
  case class Artist(reconId: ReconID) extends UpdatedRecon
  case class Album(reconId: ReconID) extends UpdatedRecon

  def fromOptionals(artistId: Option[ReconID], albumId: Option[ReconID]): UpdatedRecon = {
    require(artistId.isDefined xor albumId.isDefined, "No Recon ID present")
    if (artistId.isDefined) Artist(artistId.get) else Album(albumId.get)
  }
}
