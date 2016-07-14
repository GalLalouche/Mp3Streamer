package backend
import common.rich.primitives.RichString._

case class Url(address: String) {
  def host: Url = Url(address.split('/').apply(2))
}
