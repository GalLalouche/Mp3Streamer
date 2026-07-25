package backend.storage

import backend.recon.Artist
import backend.score.ModelScore
import models.TypeAliases.ArtistName
import slick.ast.BaseTypedType
import slick.jdbc.JdbcProfile
import slick.lifted.MappedProjection

import cats.implicits.catsSyntaxOptionId

import common.storage.ColumnMappers

private[backend] class JdbcMappers(implicit d: JdbcProfile) {
  import d.api._

  implicit val ArtistMapper: BaseTypedType[Artist] =
    MappedColumnType.base[Artist, String](_.normalize, Artist.apply)
  implicit val SongScoreMapper: BaseTypedType[ModelScore] =
    new ColumnMappers().enumeratumColumn(ModelScore)

  def artistRep(rep: Rep[ArtistName]): MappedProjection[Artist, ArtistName] =
    rep.<>(Artist.apply, _.normalize.some)
}
