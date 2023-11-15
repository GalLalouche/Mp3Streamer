package backend.scorer

import scala.concurrent.Future

import models.Song

trait ModelScorer {
  def apply(s: Song): Future[OptionalModelScore]
}
