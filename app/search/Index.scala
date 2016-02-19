package search

import models.Song

trait Index {
  protected def get(s: String): Option[Seq[Song]]  
  def find(s: String) = get(s).getOrElse(Nil)
}