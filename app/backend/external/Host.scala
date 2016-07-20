package backend.external

import backend.Url

case class Host(name: String, url: Url)
object Host {
  object Wikipedia extends Host("Wikipedia", Url("en.wikipedia.org"))
  object AllMusic extends Host("AllMusic", Url("www.allmusic.com"))
}

