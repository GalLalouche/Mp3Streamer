package backend.scorer.storage

import backend.recon.{Artist, SlickArtistReconStorage}
import backend.scorer.ModelScore
import backend.storage.{DbProvider, JdbcMappers, SlickSingleKeyColumnStorageTemplateFromConf}
import javax.inject.Inject
import slick.ast.BaseTypedType

import scala.concurrent.{ExecutionContext, Future}

import scalaz.ListT
import scalaz.Scalaz.ToFunctorOps
import common.rich.func.BetterFutureInstances._

private[backend] class ArtistScoreStorage @Inject()(
    ec: ExecutionContext,
    dbP: DbProvider,
    protected val artistStorage: SlickArtistReconStorage,
) extends SlickSingleKeyColumnStorageTemplateFromConf[Artist, ModelScore](ec, dbP)
    with StorageScorer[Artist] {
  import profile.api._

  private val mappers = new JdbcMappers()
  import mappers.{ArtistMapper, SongScoreMapper}

  private implicit val iec: ExecutionContext = ec

  override protected type Entity = (Artist, ModelScore)
  protected class Rows(tag: Tag) extends Table[Entity](tag, "artist_score") {
    def artist = column[Artist]("name", O.PrimaryKey)
    def score = column[ModelScore]("score")
    def artist_fk =
      foreignKey("artist_fk", artist, artistStorage.tableQuery)(
        _.name.mapTo[Artist],
        onUpdate = ForeignKeyAction.Cascade,
        onDelete = ForeignKeyAction.Cascade,
      )
    def * = (artist, score)
  }
  override protected type EntityTable = Rows
  override val tableQuery = TableQuery[EntityTable]
  override type Id = Artist
  override protected def btt: BaseTypedType[Artist] = ArtistMapper
  override protected def extractId(k: Artist) = k
  override protected def toId(et: Rows) = et.artist
  override protected def toEntity(k: Artist, v: ModelScore) = (k, v)
  override protected def extractValue(e: (Artist, ModelScore)) = e._2
  override def apply(a: Artist) = load(a)
  def loadAll: ListT[Future, (Artist, ModelScore)] = ListT(db.run(tableQuery.result).map(_.toList))
  override def updateScore(a: Artist, score: ModelScore) = replace(a, score).run.void
}
