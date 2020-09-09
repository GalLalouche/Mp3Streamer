package backend.lyrics.retrievers

import backend.module.StandaloneModule
import backend.recon.{AlbumReconStorage, ReconID, SlickArtistReconStorage}
import backend.storage.{DbProvider, SlickStorageTemplateFromConf}
import com.google.inject.Guice
import javax.inject.{Inject, Singleton}
import net.codingwell.scalaguice.InjectorExtensions.ScalaInjector
import slick.ast.{BaseTypedType, ScalaBaseType}
import slick.lifted.ForeignKeyQuery

import scala.concurrent.{ExecutionContext, Future}

@Singleton
private class SlickInstrumentalArtistStorage @Inject()(
    ec: ExecutionContext,
    dbP: DbProvider,
    protected val artistStorage: SlickArtistReconStorage
) extends SlickStorageTemplateFromConf[String, Unit](ec, dbP) with InstrumentalArtistStorage {
  import profile.api._

  override protected type Id = String
  override protected implicit def btt: BaseTypedType[Id] = ScalaBaseType.stringType
  override protected type Entity = String
  protected class ArtistTable(tag: Tag) extends Table[Entity](tag, "instrumental_artist") {
    def name = column[String]("name", O.PrimaryKey)
    def name_fk = foreignKey("name_fk", name, artistStorage.tableQuery)(
      _.name,
      onUpdate = ForeignKeyAction.Cascade,
      onDelete = ForeignKeyAction.Cascade,
    )
    def * = name
  }
  override protected type EntityTable = ArtistTable
  override protected val tableQuery = TableQuery[EntityTable]
  override protected def toEntity(k: String, v: Unit) = extractId(k)
  override protected def extractId(k: String) = k.toLowerCase
  override protected def toId(et: ArtistTable) = et.name
  override protected def extractValue(e: String): Unit = ()

  // TODO SetStorage
  override def store(artistName: String): Future[Unit] = store(artistName: String, ())
}

object SlickInstrumentalArtistStorage {
  def main(args: Array[String]): Unit = {
    import common.rich.RichFuture._
    val injector = Guice.createInjector(StandaloneModule)
    implicit val ec: ExecutionContext = injector.instance[ExecutionContext]
    injector.instance[SlickInstrumentalArtistStorage].store("bladgfasdfasdf092ejalsdkjasd").get
  }
}
