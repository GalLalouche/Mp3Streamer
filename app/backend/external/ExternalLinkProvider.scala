package backend.external

import backend.recon.ReconID
import backend.storage.Retriever

trait ExternalLinkProvider extends Retriever[ReconID, Traversable[ExternalLink]]
