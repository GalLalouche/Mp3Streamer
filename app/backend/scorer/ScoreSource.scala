package backend.scorer

sealed trait ScoreSource
object ScoreSource {
  case object Artist extends ScoreSource
  case object Album extends ScoreSource
  case object Song extends ScoreSource
}
