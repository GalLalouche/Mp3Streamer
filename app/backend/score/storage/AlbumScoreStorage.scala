package backend.score.storage

import backend.recon.{Album, Artist, SlickArtistReconStorage, YearlessAlbum}
import backend.score.ModelScore
import backend.storage.{DbProvider, JdbcMappers, SlickStorageTemplateFromConf}
import com.google.inject.Inject
import models.AlbumTitle

import scala.concurrent.{ExecutionContext, Future}

import common.rich.func.BetterFutureInstances._
import scalaz.ListT

import common.rich.RichT.richT

private[score] class AlbumScoreStorage @Inject() (
    protected override val ec: ExecutionContext,
    dbP: DbProvider,
    protected val artistStorage: SlickArtistReconStorage,
) extends SlickStorageTemplateFromConf[Album, ModelScore](ec, dbP)
    with StorageScorer[Album] {
  import profile.api._

  private val mappers = new JdbcMappers()
  import mappers.{ArtistMapper, SongScoreMapper}

  private implicit val iec: ExecutionContext = ec

  // TODO can this use YearlessAlbum?
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
  protected override def toEntity(k: Album, v: ModelScore) = (k.artist, k.title.toLowerCase, v)
  protected override def extractValue(e: (Artist, AlbumTitle, ModelScore)) = e._3
  // TODO use an AlbumTitle case class or CIString?
  protected override def keyFilter(k: Album)(e: Rows) =
    e.artist === k.artist && e.title === k.title.toLowerCase
  def loadAll: ListT[Future, (YearlessAlbum, ModelScore)] =
    ListT(db.run(tableQuery.result).map(_.toList)).map(e => YearlessAlbum(e._2, e._1) -> e._3)
  def allForArtist(a: Artist): Future[Seq[(AlbumTitle, ModelScore)]] =
    db.run(tableQuery.filter(_.artist === a).map(_.toTuple(_.title, _.score)).result)
}
