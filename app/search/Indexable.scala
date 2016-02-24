package search

import models.Song

trait Indexable[T] {
  def terms(t: T): Seq[String]
  def extractFromSong(s: Song): T
  def compare(t1: T, t2: T): Boolean
}