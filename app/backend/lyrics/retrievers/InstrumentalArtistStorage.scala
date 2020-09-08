package backend.lyrics.retrievers

import backend.storage.{DbProvider, SlickStorageTemplateFromConf}
import javax.inject.{Inject, Singleton}
import slick.ast.{BaseTypedType, ScalaBaseType}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
private[lyrics] class InstrumentalArtistStorage @Inject()(
    ec: ExecutionContext,
    dbP: DbProvider,
) extends SlickStorageTemplateFromConf[String, Unit](ec, dbP) {
  import profile.api._

  override protected type Id = String
  override protected implicit def btt: BaseTypedType[Id] = ScalaBaseType.stringType
  override protected type Entity = String
  protected class ArtistTable(tag: Tag) extends Table[Entity](tag, "instrumental_artist") {
    def name = column[String]("name", O.PrimaryKey)
    def * = name
  }
  override protected type EntityTable = ArtistTable
  override protected val tableQuery = TableQuery[EntityTable]
  override protected def toEntity(k: String, v: Unit) = extractId(k)
  override protected def extractId(k: String) = k.toLowerCase
  override protected def toId(et: ArtistTable) = et.name
  override protected def extractValue(e: String): Unit = ()

  def store(artistName: String): Future[Unit] = store(artistName: String, ())
}
