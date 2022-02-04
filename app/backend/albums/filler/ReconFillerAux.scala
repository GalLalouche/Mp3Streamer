package backend.albums.filler

import backend.recon.ReconID

import scala.concurrent.Future

trait ReconFillerAux[R] {
  def musicBrainzPath: String
  def prettyPrint(r: R): String
  def verify(r: R, id: ReconID): Future[Boolean]
}

