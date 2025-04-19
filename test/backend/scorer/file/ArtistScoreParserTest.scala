package backend.scorer.file

import backend.recon.Artist
import backend.recon.ReconcilablesTest.arbitraryArtist

class ArtistScoreParserTest extends ScoreParserTestTemplate[Artist] {
  protected override val basicInput: (Artist, String) =
    (Artist("Blind Guardian"), "*** ARTIST ; Blind Guardian")
  protected override val parse = ArtistScoreParser(_).get
  protected override val format = OrgScoreFormatter.artist
}
