package backend.storage

import backend.recon.Artist
import backend.score.ModelScore
import slick.ast.BaseTypedType
import slick.jdbc.JdbcProfile

import common.storage.ColumnMappers

private[backend] class JdbcMappers(implicit d: JdbcProfile) {
  import d.api._

  implicit val ArtistMapper: BaseTypedType[Artist] =
    MappedColumnType.base[Artist, String](_.normalize, Artist.apply)
  implicit val SongScoreMapper: BaseTypedType[ModelScore] =
    new ColumnMappers().enumeratumColumn(ModelScore)
}
