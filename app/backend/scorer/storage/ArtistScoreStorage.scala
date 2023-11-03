package backend.scorer.storage

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scalaz.ListT
import scalaz.Scalaz.ToFunctorOps

import backend.recon.{Artist, SlickArtistReconStorage}
import backend.scorer.ModelScore
import backend.storage.{DbProvider, JdbcMappers, SlickSingleKeyColumnStorageTemplateFromConf}
import common.rich.func.BetterFutureInstances._
import slick.ast.BaseTypedType

private[backend] class ArtistScoreStorage @Inject() (
    ec: ExecutionContext,
    dbP: DbProvider,
    protected val artistStorage: SlickArtistReconStorage,
) extends SlickSingleKeyColumnStorageTemplateFromConf[Artist, ModelScore](ec, dbP)
    with StorageScorer[Artist] {
  import profile.api._

  private val mappers = new JdbcMappers()
  import mappers.{ArtistMapper, SongScoreMapper}

  private implicit val iec: ExecutionContext = ec

  protected override type Entity = (Artist, ModelScore)
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
  protected override type EntityTable = Rows
  override val tableQuery = TableQuery[EntityTable]
  override type Id = Artist
  protected override def btt: BaseTypedType[Artist] = ArtistMapper
  protected override def extractId(k: Artist) = k
  protected override def toId(et: Rows) = et.artist
  protected override def toEntity(k: Artist, v: ModelScore) = (k, v)
  protected override def extractValue(e: (Artist, ModelScore)) = e._2
  override def apply(a: Artist) = load(a)
  def loadAll: ListT[Future, (Artist, ModelScore)] = ListT(db.run(tableQuery.result).map(_.toList))
  override def updateScore(a: Artist, score: ModelScore) = replace(a, score).run.void
}
