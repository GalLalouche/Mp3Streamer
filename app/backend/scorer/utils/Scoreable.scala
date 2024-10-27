package backend.scorer.utils

import javax.inject.Inject

import backend.recon.Reconcilable.SongExtractor
import backend.scorer.CachedModelScorer
import models.{Album, Artist, Song}

trait Scoreable[A] {
  type Child
  def isEmpty(a: A): Boolean = children(a).isEmpty
  def scoreFormat(a: A): String
  def childrenEv: Scoreable[Child]
  def children(a: A): Seq[Child]
}

object Scoreable {
  implicit class ScoreableOps[A](a: A)(implicit ev: Scoreable[A]) {
    def isEmpty: Boolean = ev.isEmpty(a)
    def scoreFormat: String = ev.scoreFormat(a)
  }

  class ScoreableImpl @Inject() (
      scorer: CachedModelScorer,
  ) {
    def artist: Scoreable[Artist] = new Scoreable[Artist] {
      override type Child = Album
      override def childrenEv: Scoreable[Album] = album
      override def children(a: Artist): Seq[Album] = a.albums
      override def scoreFormat(a: Artist): String =
        OrgScoreFormatter.artist(a.toRecon, scorer.explicitScore(a.toRecon))
    }
    def album: Scoreable[Album] = new Scoreable[Album] {
      override type Child = Song
      override def childrenEv: Scoreable[Song] = song
      override def children(a: Album): Seq[Song] = a.songs
      override def scoreFormat(a: Album): String =
        OrgScoreFormatter.album(a.toRecon, scorer.explicitScore(a.toRecon))
    }
    def song: Scoreable[Song] = new Scoreable[Song] {
      override type Child = Nothing
      override def childrenEv: Scoreable[Nothing] = throw new UnsupportedOperationException()
      override def children(s: Song): Seq[Nothing] = Nil
      override def scoreFormat(s: Song): String =
        OrgScoreFormatter.song(s, scorer.explicitScore(s.track))
    }
  }
}
