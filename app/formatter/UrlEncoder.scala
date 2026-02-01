package formatter

import models.Song

import common.path.ref.PathRef

trait UrlEncoder {
  def apply(s: String): String
  def apply(p: PathRef): String = apply(p.path)
  def apply(s: Song): String = apply(s.file)
}
