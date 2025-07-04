package backend.score.file

import backend.recon.Reconcilable.SongExtractor
import backend.score.CachedModelScorer
import com.google.inject.Inject
import models.{AlbumDir, ArtistDir, Song}

private trait Scoreable[A] {
  type Child
  def isEmpty(a: A): Boolean = children(a).isEmpty
  def scoreFormat(a: A): String
  def childrenEv: Scoreable[Child]
  def children(a: A): Seq[Child]
}

private object Scoreable {
  implicit class ScoreableOps[A](a: A)(implicit ev: Scoreable[A]) {
    def isEmpty: Boolean = ev.isEmpty(a)
    def scoreFormat: String = ev.scoreFormat(a)
  }

  class ScoreableImpl @Inject() (scorer: CachedModelScorer) {
    def artistDir: Scoreable[ArtistDir] = new Scoreable[ArtistDir] {
      override type Child = AlbumDir
      override def childrenEv: Scoreable[AlbumDir] = albumDir
      override def children(a: ArtistDir): Seq[AlbumDir] = a.albums
      override def scoreFormat(a: ArtistDir): String =
        OrgScoreFormatter.artist(a.toRecon, scorer.explicitScore(a.toRecon))
    }
    def albumDir: Scoreable[AlbumDir] = new Scoreable[AlbumDir] {
      override type Child = Song
      override def childrenEv: Scoreable[Song] = track
      override def children(a: AlbumDir): Seq[Song] = a.songs
      override def scoreFormat(a: AlbumDir): String =
        OrgScoreFormatter.album(a.toRecon, scorer.explicitScore(a.toRecon))
    }
    def track: Scoreable[Song] = new Scoreable[Song] {
      override type Child = Nothing
      override def childrenEv: Scoreable[Nothing] = throw new UnsupportedOperationException()
      override def children(s: Song): Seq[Nothing] = Nil
      override def scoreFormat(s: Song): String = {
        val track = s.track
        OrgScoreFormatter.track(track, scorer.explicitScore(track))
      }
    }
  }
}
