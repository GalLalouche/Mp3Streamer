package backend.storage

import backend.recon.Artist
import backend.scorer.ModelScore
import common.storage.ColumnMappers
import slick.ast.BaseTypedType
import slick.jdbc.JdbcProfile

private[backend] class JdbcMappers(implicit d: JdbcProfile) {
  import d.api._

  implicit val ArtistMapper: BaseTypedType[Artist] =
    MappedColumnType.base[Artist, String](_.normalize, Artist.apply)
  implicit val SongScoreMapper: BaseTypedType[ModelScore] =
    new ColumnMappers().enumeratumColumn(ModelScore)
}
