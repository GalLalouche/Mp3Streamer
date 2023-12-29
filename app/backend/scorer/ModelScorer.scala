package backend.scorer

import models.Song

import scala.concurrent.Future

trait ModelScorer {
  def apply(s: Song): Future[OptionalModelScore]
}
