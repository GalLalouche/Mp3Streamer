package backend.scorer

import backend.recon.{Album, Artist, ReconcilableFactory}
import backend.scorer.AlbumScoreToOrg._
import javax.inject.Inject
import models.{Genre, GenreFinder}

import scala.concurrent.ExecutionContext

import common.io.IODirectory
import common.rich.RichT._

/**
* Creates an .org file for faster updating of artists and albums.
* See [[ScoreParser]] for the parser of the output file.
*/
private class AlbumScoreToOrg @Inject()(
    scorer: CachedModelScorer,
    reconcilableFactory: ReconcilableFactory,
    enumGenreFinder: GenreFinder,
    ec: ExecutionContext,
) {
  private implicit val iec: ExecutionContext = ec

  def go(scope: Scope, update: Update): Seq[String] = {
    def goArtist(artist: Artist, albums: TraversableOnce[Album]): Seq[String] = scope match {
      case Scope.ArtistsOnly =>
        val score = scorer(artist) getOrElse ModelScore.Default
        if (update filterScore score)
          Vector(scoreString(2, "ARTIST ; " ++ artist.name, score))
        else
          Nil
      case Scope.ArtistsAndAlbums =>
        val artistScore = scorer(artist) getOrElse ModelScore.Default
        val albumScores = albums
            .toVector
            .sortBy(_.year)
            .map(e => (s"${e.year} ${e.title}", scorer(e) getOrElse ModelScore.Default))
        scoreString(2, artist.name, artistScore) ::
            albumScores.view
                .filter(update filterScore _._2)
                .map(e => scoreString(3, e._1, e._2))
                .toList
    }

    // TODO further nest into rock/metal and subgenres
    def goGenre(g: Genre, artists: Map[Artist, Iterable[Album]]): Seq[String] =
      artists
          .toVector
          .sortBy(_._1.name)
          .flatMap(Function.tupled(goArtist))
          .mapIf(_.nonEmpty).to(s"* $g" +: _)

    reconcilableFactory.albumDirectories
        .groupBy(d => enumGenreFinder.apply(d.asInstanceOf[IODirectory]))
        .mapValues(_.map(reconcilableFactory.toAlbum(_).get).groupBy(_.artist))
        .toVector
        .sortBy(_._1)
        .flatMap(Function.tupled(goGenre))
  }
}

private object AlbumScoreToOrg {
  sealed trait Scope
  object Scope {
    case object ArtistsOnly extends Scope
    case object ArtistsAndAlbums extends Scope
  }
  sealed trait Update {
    def filterScore(s: ModelScore): Boolean = this match {
      case Update.DefaultOnly => s == ModelScore.Default
      case Update.All => true
    }
  }
  object Update {
    case object DefaultOnly extends Update
    case object All extends Update
  }

  private def scoreString(depth: Int, name: String, score: ModelScore): String =
    s"${"*" * depth} $name === $score"
}
