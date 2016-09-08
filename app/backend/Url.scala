package backend

case class Url(address: String) {
  require(!address.matches("\\s*"), "empty address")
  def host = Url(
    if (address startsWith "http")
      address split '/' apply 2
    else
      address takeWhile (_ != '/'))
  def +/(s: String): Url =
    if (address.last == '/' || s.head == '/')
      Url(address + (if (address.last == s.head) s.drop(1) else s))
    else
      Url(s"$address/$s")
}
