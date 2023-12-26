package models

sealed class Genre(val name: String) extends Ordered[Genre] {
  private def isFlat = this match {
    case Genre.Jazz => true
    case Genre.Blues => true
    case Genre.Musicals => true
    case Genre.NewAge => true
    case Genre.Classical => true
    case Genre.Rock(subgenre) => false
    case Genre.Metal(subgenre) => false
    case _ => throw new AssertionError()
  }
  override def compare(that: Genre): Int = {
    import Genre._
    (this.isFlat, that.isFlat) match {
      case (true, true) => this.name.compare(that.name)
      case (true, false) => 1
      case (false, true) => -1
      case (false, false) =>
        (this, that) match {
          case (Rock(_), Metal(_)) => -1
          case (Metal(_), Rock(_)) => 1
          case (Rock(n1), Rock(n2)) => n1.compare(n2)
          case (Metal(n1), Metal(n2)) => n1.compare(n2)
          case _ => throw new AssertionError()
        }
    }
  }
}

object Genre extends {
  case object Blues extends Genre("Blues")
  case object Jazz extends Genre("Jazz")
  case object Musicals extends Genre("Musicals")
  case object NewAge extends Genre("New Age")
  case object Classical extends Genre("Classical")
  case class Rock(subgenre: String) extends Genre(subgenre)
  case class Metal(subgenre: String) extends Genre(subgenre)
}
