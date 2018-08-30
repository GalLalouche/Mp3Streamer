package models

import common.io.DirectoryRef
import monocle.macros.Lenses

@Lenses
case class Album(dir: DirectoryRef, title: String, artistName: String, year: Int, songs: Seq[Song])
