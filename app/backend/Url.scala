package backend

case class Url(address: String) {
  require(!address.matches("\\s*"), "empty address")
  def host: Url = Url(address.split('/').apply(2))
}
