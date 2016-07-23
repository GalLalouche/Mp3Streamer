package backend

case class Url(address: String) {
  def host: Url = Url(address.split('/').apply(2))
}
