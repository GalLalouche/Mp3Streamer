package mains.fixer.new_artist

private sealed trait Direction
private object Direction {
  case object Previous extends Direction
  case object Next extends Direction
}
