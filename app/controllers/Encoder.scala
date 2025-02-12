package controllers

import models.Song

import common.io.PathRef

trait Encoder {
  def apply(s: String): String
  def apply(p: PathRef): String = apply(p.path)
  def apply(s: Song): String = apply(s.file)
}
