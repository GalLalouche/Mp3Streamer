package backend.scorer

import backend.recon.{Album, Artist, SlickAlbumReconStorage}
import backend.storage.{DbProvider, SlickStorageTemplateFromConf}
import javax.inject.Inject

import scala.concurrent.{ExecutionContext, Future}

import scalaz.ListT

private class AlbumScoreStorage @Inject()(
    ec: ExecutionContext,
    dbP: DbProvider,
    protected val albumStorage: SlickAlbumReconStorage,
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
    def pk = primaryKey("pk", (artist, title))
    def album_fk =
      foreignKey("album_fk", (artist, title), albumStorage.tableQuery)(
        e => (e.artist.mapTo[Artist], e.album),
        onUpdate = ForeignKeyAction.Cascade,
        onDelete = ForeignKeyAction.Cascade,
      )
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
}
