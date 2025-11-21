package backend.lyrics.retrievers

import backend.recon.Artist
import com.google.inject.ImplementedBy

import common.storage.SetStorage

@ImplementedBy(classOf[InstrumentalArtistStorageImpl])
trait InstrumentalArtistStorage extends SetStorage[Artist]
