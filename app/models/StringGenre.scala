package models

/** Used by [[EnumGenre]] to provide hard-coded genres.
*
* This class is more flexible, since it only assumes flat and nested genres, and not specific genres.
* However, this isn't as useful when actually writing business code, e.g., to create different kinds of
* folders.
*/
private sealed trait StringGenre
private object StringGenre {
  case class Flat(name: String) extends StringGenre // e.g., Musicals
  case class Nested(top: String, sub: String) extends StringGenre // e.g., Metal/Black Metal
}
