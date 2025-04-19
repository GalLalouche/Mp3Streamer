package backend.scorer.file

import backend.recon.{Album, Artist}
import backend.recon.ReconcilablesTest.arbitraryAlbum

class AlbumScoreParserTest extends ScoreParserTestTemplate[Album] {
  protected override val basicInput: (Album, String) =
    (
      Album("A Night at the Opera", 2002, Artist("Blind Guardian")),
      "** ALBUM ; Blind Guardian ;;; A Night at the Opera (2002)",
    )
  protected override val parse = AlbumScoreParser(_).get
  protected override val format = OrgScoreFormatter.album
}
