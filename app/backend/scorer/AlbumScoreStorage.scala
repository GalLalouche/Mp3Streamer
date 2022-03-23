package backend.scorer

import backend.recon.{Album, Artist, SlickArtistReconStorage}
import backend.storage.{DbProvider, SlickStorageTemplateFromConf}
import javax.inject.Inject

import scala.concurrent.{ExecutionContext, Future}

import scalaz.ListT
import scalaz.Scalaz.ToFunctorOps
import common.rich.func.BetterFutureInstances._

private class AlbumScoreStorage @Inject()(
    ec: ExecutionContext,
    dbP: DbProvider,
    protected val artistStorage: SlickArtistReconStorage,
) extends SlickStorageTemplateFromConf[Album, ModelScore](ec, dbP)
    with StorageScorer[Album] {
  import profile.api._

  private val mappers = new JdbcMappers()
  import mappers.{ArtistMapper, SongScoreMapper}

  private implicit val iec: ExecutionContext = ec

  type AlbumTitle = String
  override protected type Entity = (Artist, AlbumTitle, ModelScore)
  protected class Rows(tag: Tag) extends Table[Entity](tag, "album_score") {
    def artist = column[Artist]("artist")
    def title = column[AlbumTitle]("title")
    def score = column[ModelScore]("score")
    def pk = primaryKey(dbP.constraintMangler("pk"), (artist, title))
    def artist_fk =
      foreignKey(
        dbP.constraintMangler("artist_fk"), artist, artistStorage.tableQuery)(
        _.name.mapTo[Artist])
    def * = (artist, title, score)
  }
  override protected type EntityTable = Rows
  override protected val tableQuery = TableQuery[EntityTable]
  override protected def toEntity(k: Album, v: ModelScore) = (k.artist, k.title, v)
  override protected def extractValue(e: (Artist, AlbumTitle, ModelScore)) = e._3
  override protected def keyFilter(k: Album)(e: Rows) = e.artist === k.artist && e.title === k.title
  override def apply(a: Album) = load(a)
  def loadAll: ListT[Future, (Artist, AlbumTitle, ModelScore)] =
    ListT(db.run(tableQuery.result).map(_.toList))
  override def updateScore(a: Album, score: ModelScore) = replace(a, score).run.void
}
