package backend.score.file

import backend.recon.Reconcilable.SongExtractor
import backend.recon.ReconcilablesTest.arbitraryTrack
import backend.recon.Track
import models.FakeModelFactory

class TrackScoreParserTest extends ScoreParserTestTemplate[Track] {
  private val factory = new FakeModelFactory()
  protected override val basicInput: (Track, String) =
    (
      factory
        .song(
          title = "And then There was Silence",
          albumName = "A Night at the Opera",
          year = 2002,
          artistName = "Blind Guardian",
        )
        .track,
      "SONG ; Blind Guardian ;;;  A Night at the Opera (2002) ;;; 10. And then There was Silence",
    )
  protected override val parse = TrackScoreParser(_).get
  protected override val format = OrgScoreFormatter.track
}
