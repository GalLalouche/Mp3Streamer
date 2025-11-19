package backend.score.storage

import backend.recon.{Artist, SlickArtistReconStorage}
import backend.score.ModelScore
import backend.storage.{DbProvider, JdbcMappers, SlickSingleKeyColumnStorageTemplateFromConf}
import com.google.inject.Inject
import slick.ast.BaseTypedType

import scala.concurrent.ExecutionContext

private[backend] class ArtistScoreStorage @Inject() (
    protected override val ec: ExecutionContext,
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
}
