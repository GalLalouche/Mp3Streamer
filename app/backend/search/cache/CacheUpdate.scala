package backend.search.cache

import common.io.DirectoryRef

private[search] case class CacheUpdate(currentIndex: Int, totalNumber: Int, dir: DirectoryRef)
