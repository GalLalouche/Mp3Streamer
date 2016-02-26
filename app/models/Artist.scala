package models

import common.rich.path.Directory

final class Artist(val name: String, val albums: Set[Album]) {
  override def equals(other: Any) = other match {
    case that: Artist => name == that.name
    case _            => false
  }

  override def hashCode() = 41 + name.hashCode
}