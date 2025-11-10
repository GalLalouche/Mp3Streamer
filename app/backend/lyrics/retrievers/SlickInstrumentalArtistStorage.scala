package backend.lyrics.retrievers

import backend.module.StandaloneModule
import backend.recon.{Artist, SlickArtistReconStorage}
import backend.storage.{DbProvider, IsomorphicSlickStorage}
import com.google.inject.{Guice, Inject, Singleton}
import models.TypeAliases.ArtistName
import net.codingwell.scalaguice.InjectorExtensions.ScalaInjector
import slick.ast.{BaseTypedType, ScalaBaseType}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
private class SlickInstrumentalArtistStorage @Inject() (
    ec: ExecutionContext,
    dbP: DbProvider,
    protected val artistStorage: SlickArtistReconStorage,
) extends IsomorphicSlickStorage[Artist, Unit]()(ec, dbP)
    with InstrumentalArtistStorage {
  import profile.api._

  protected override type Id = ArtistName
  protected implicit override def btt: BaseTypedType[Id] = ScalaBaseType.stringType
  protected override type Entity = ArtistName
  protected class ArtistTable(tag: Tag) extends Table[Entity](tag, "instrumental_artist") {
    def name = column[ArtistName]("name", O.PrimaryKey)
    def name_fk = foreignKey("name_fk", name, artistStorage.tableQuery)(
      _.name,
      onUpdate = ForeignKeyAction.Cascade,
      onDelete = ForeignKeyAction.Cascade,
    )
    def * = name
    def artist = name.mapTo[Artist]
  }
  protected override type EntityTable = ArtistTable
  protected override val tableQuery = TableQuery[EntityTable]
  protected override def extractKey(e: ArtistName): Artist = Artist(e)
  protected override def extractId(k: Artist): ArtistName = k.normalize
  protected override def toEntity(k: Artist, v: Unit): ArtistName = k.normalize
  protected override def toId(et: ArtistTable) = et.name
  protected override def extractValue(e: ArtistName): Unit = ()

  // TODO SetStorage, i.e., a storage only for check if something exists or not
  override def store(artist: Artist): Future[Unit] = store(artist: Artist, ())
}
