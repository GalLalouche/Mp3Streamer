package backend.scorer.utils.foobar

import backend.albums.filler.ArtistReconPusher
import backend.logging.Logger
import backend.mb.MbArtistReconciler
import backend.recon.{Artist, ArtistReconStorage}
import backend.recon.Reconcilable.SongExtractor
import backend.scorer.{FullInfoModelScorer, FullInfoScore, ModelScore, ScoreSource}
import javax.inject.Inject
import models.{Song, SongTagParser}

import java.io.File
import scala.concurrent.{ExecutionContext, Future}

import scalafx.scene.Node
import scalafx.scene.control.{ComboBox, Label}
import scalafx.scene.layout.{GridPane, Pane, VBox}
import scalafx.scene.text.{Text, TextFlow}

import scalaz.syntax.bind._
import common.rich.func.BetterFutureInstances._
import common.rich.func.ToMoreFunctorOps.toMoreFunctorOps
import common.rich.func.ToMoreMonadErrorOps.toMoreMonadErrorOps

import common.rich.RichT.richT
import common.rich.path.RichFile.richFile
import common.rich.RichFuture.richFuture
import common.scalafx.Builders
import common.scalafx.RichNode.richNode

private class FoobarScorer @Inject()(
    reconciler: MbArtistReconciler,
    reconStorage: ArtistReconStorage,
    pusher: ArtistReconPusher,
    scorer: FullInfoModelScorer,
    ec: ExecutionContext,
    logger: Logger,
) {
  import FoobarScorer._

  private implicit val iec: ExecutionContext = ec
  // Uses the "Now Playing Simple" plugin, which writes the currently playing song to a file.
  private def currentlyPlayingSong(nowPlayingSimpleOutput: File): Future[Song] =
    Future(SongTagParser(new File(nowPlayingSimpleOutput.readAll)))
  private def makeScore(song: Song, source: ScoreSource, score: String, onScoreChange: () => Any): Node = {
    assert(comboValues.contains(score))
    val $ = new ComboBox[String](comboValues)
    $.value = score
    $.onAction = _ => {
      val value = $.value.value
      ModelScore.withNameOption(value) match {
        case Some(newScore) =>
          val songSummary = source match {
            case ScoreSource.Artist => song.artistName
            case ScoreSource.Album => s"${song.artistName} - ${song.albumName}"
            case ScoreSource.Song => s"${song.artistName} - ${song.title}"
          }
          println(s"Updating <$source> score for <$songSummary> to <$newScore>")
          (source match {
            case ScoreSource.Artist => scorer.updateArtistScore(song, newScore)
            case ScoreSource.Album => scorer.updateAlbumScore(song, newScore)
            case ScoreSource.Song => scorer.updateSongScore(song, newScore)
          }).toTry
              .listenError(logger.error("Failed to update score", _))
              .>|(onScoreChange())
        case None =>
          assert(value == ModelScore.DefaultTitle)
          println(s"Skipping <$value> score")
          if (value != score) // reset score to old value.
            $.setValue(score)
      }
    }
    $
  }
  // TODO this whole logic need to be abstracted away :\
  private def reconcileArtist(a: Artist): Future[_] = reconStorage
      .exists(a)
      .ifM(
        Future.successful(Unit),
        reconciler(a)
            .listen(_ => logger.info(s"Reconciled <$a>"))
            .flatMapF(r => pusher.withValidation(a.name, r.id, isIgnored = false))
            .run
            .void
      )
  def update(nowPlayingSimpleOutput: File, onScoreChange: () => Any): Future[Pane] = for {
    song <- currentlyPlayingSong(nowPlayingSimpleOutput)
    _ <- reconcileArtist(song.artist)
    score <- scorer(song)
  } yield {
    val nullableScore = makeNullableSongScore(score)
    val individualScores: Pane = new GridPane {
      hgap = 10
      vgap = 2
      Vector(
        (ScoreSource.Song, song.title, nullableScore.songScore),
        (ScoreSource.Album, song.albumName, nullableScore.albumScore),
        (ScoreSource.Artist, song.artistName, nullableScore.artistScore),
      ).zipWithIndex.foreach {case ((source, title, score), i) => addRow(
        i,
        new Label(s"$source: "),
        new Text(title).makeBold(),
        makeScore(song, source, score, onScoreChange),
      )
      }
    }
    val finalScore = new TextFlow(
      new Text("Final score: "),
      new Text(score.toModelScore.orDefaultString).makeBold(),
      new Text(s" (${nullableScore.source})"),
    )
    new VBox {
      spacing = 10
      children = Vector(finalScore, individualScores).<|(_.foreach(_.margin = insets))
    }.<|(_ setFontSize 14)
  }
}

private object FoobarScorer {
  private case class NullableSongScore(
      score: String,
      source: String,
      songScore: String,
      albumScore: String,
      artistScore: String,
  )
  private def makeNullableSongScore(score: FullInfoScore) = score match {
    case FullInfoScore.Default => NullableSongScore(
      score = ModelScore.DefaultTitle,
      source = "N/A",
      songScore = ModelScore.DefaultTitle,
      albumScore = ModelScore.DefaultTitle,
      artistScore = ModelScore.DefaultTitle,
    )
    case FullInfoScore.Scored(score, source, songScore, albumScore, artistScore) => NullableSongScore(
      score = score.toString,
      source = source.toString,
      songScore = songScore.orDefaultString,
      albumScore = albumScore.orDefaultString,
      artistScore = artistScore.orDefaultString,
    )
  }
  private val comboValues = ModelScore.DefaultTitle +: ModelScore.values.map(_.toString)
  private val insets = Builders.insets(left = 5)
}
