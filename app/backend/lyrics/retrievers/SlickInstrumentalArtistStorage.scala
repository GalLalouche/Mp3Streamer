package backend.lyrics.retrievers

import javax.inject.{Inject, Singleton}

import backend.module.StandaloneModule
import backend.recon.SlickArtistReconStorage
import backend.storage.{DbProvider, SlickSingleKeyColumnStorageTemplateFromConf}
import com.google.inject.Guice
import net.codingwell.scalaguice.InjectorExtensions.ScalaInjector
import slick.ast.{BaseTypedType, ScalaBaseType}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
private class SlickInstrumentalArtistStorage @Inject() (
    ec: ExecutionContext,
    dbP: DbProvider,
    protected val artistStorage: SlickArtistReconStorage,
) extends SlickSingleKeyColumnStorageTemplateFromConf[String, Unit](ec, dbP)
    with InstrumentalArtistStorage {
  import profile.api._

  protected override type Id = String
  protected implicit override def btt: BaseTypedType[Id] = ScalaBaseType.stringType
  protected override type Entity = String
  protected class ArtistTable(tag: Tag) extends Table[Entity](tag, "instrumental_artist") {
    def name = column[String]("name", O.PrimaryKey)
    def name_fk = foreignKey("name_fk", name, artistStorage.tableQuery)(
      _.name,
      onUpdate = ForeignKeyAction.Cascade,
      onDelete = ForeignKeyAction.Cascade,
    )
    def * = name
  }
  protected override type EntityTable = ArtistTable
  protected override val tableQuery = TableQuery[EntityTable]
  protected override def toEntity(k: String, v: Unit) = extractId(k)
  protected override def extractId(k: String) = k.toLowerCase
  protected override def toId(et: ArtistTable) = et.name
  protected override def extractValue(e: String): Unit = ()

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
