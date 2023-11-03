package backend.albums.filler

import scala.concurrent.Future

import backend.recon.ReconID

trait ReconFillerAux[R] {
  def musicBrainzPath: String
  def prettyPrint(r: R): String
  def verify(r: R, id: ReconID): Future[Boolean]
}
