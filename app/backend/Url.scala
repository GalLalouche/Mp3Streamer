package backend

case class Url(address: String) {
  require(!address.matches("\\s*"), "empty address")
  def host = Url(
    if (address startsWith "http")
      address split '/' apply 2
    else
      address takeWhile (_ != '/'))

}
