package backend.scorer.storage

import javax.inject.Inject

import backend.recon.{Album, Artist, SlickArtistReconStorage}
import backend.scorer.ModelScore
import backend.storage.{DbProvider, JdbcMappers, SlickStorageTemplateFromConf}
import models.Album.AlbumTitle

import scala.concurrent.{ExecutionContext, Future}

import scalaz.ListT

private[scorer] class AlbumScoreStorage @Inject() (
    protected override val ec: ExecutionContext,
    dbP: DbProvider,
    protected val artistStorage: SlickArtistReconStorage,
) extends SlickStorageTemplateFromConf[Album, ModelScore](ec, dbP)
    with StorageScorer[Album] {
  import profile.api._

  private val mappers = new JdbcMappers()
  import mappers.{ArtistMapper, SongScoreMapper}

  private implicit val iec: ExecutionContext = ec

  protected override type Entity = (Artist, AlbumTitle, ModelScore)
  protected class Rows(tag: Tag) extends Table[Entity](tag, "album_score") {
    def artist = column[Artist]("artist")
    def title = column[AlbumTitle]("title")
    def score = column[ModelScore]("score")
    def pk = primaryKey(dbP.constraintMangler("pk"), (artist, title))
    def artist_fk =
      foreignKey(dbP.constraintMangler("artist_fk"), artist, artistStorage.tableQuery)(
        _.name.mapTo[Artist],
      )
    def * = (artist, title, score)
  }
  protected override type EntityTable = Rows
  protected override val tableQuery = TableQuery[EntityTable]
  protected override def toEntity(k: Album, v: ModelScore) = (k.artist, k.title, v)
  protected override def extractValue(e: (Artist, AlbumTitle, ModelScore)) = e._3
  protected override def keyFilter(k: Album)(e: Rows) = e.artist === k.artist && e.title === k.title
  def loadAll: ListT[Future, (Artist, AlbumTitle, ModelScore)] =
    ListT(db.run(tableQuery.result).map(_.toList))
}
