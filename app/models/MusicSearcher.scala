package models

trait MusicSearcher extends (String => Seq[Song]) {}