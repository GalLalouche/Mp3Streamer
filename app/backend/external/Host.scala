package backend.external

import backend.Url

case class Host(name: String, url: Url)
object Host {
  object Wikipedia extends Host("wikipedia", Url("en.wikipedia.org"))
  object AllMusic extends Host("allMusic", Url("www.allmusic.com"))
}

