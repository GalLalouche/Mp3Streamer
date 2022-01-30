package models

import models.MusicFinder.Genre

// Not really a proper enum, due to subgenres
sealed class EnumGenre(val name: String)
object EnumGenre extends {
  case object Jazz extends EnumGenre("Jazz")
  case object Musicals extends EnumGenre("Musicals")
  case object NewAge extends EnumGenre("New Age")
  case object Classical extends EnumGenre("Classical")
  case class Rock(subgenre: String) extends EnumGenre(subgenre)
  case class Metal(subgenre: String) extends EnumGenre(subgenre)

  def from(g: MusicFinder.Genre): EnumGenre = g match {
    case Genre.Flat(name) => name match {
      case "Jazz" => Jazz
      case "Musicals" => Musicals
      case "New Age" => NewAge
      case "Classical" => Classical
      case e => throw new NoSuchElementException(e)
    }
    case Genre.Nested(top, sub) => top.toLowerCase match {
      case "rock" => Rock(sub)
      case "metal" => Metal(sub)
      case _ => throw new NoSuchElementException(top)
    }
  }
}
