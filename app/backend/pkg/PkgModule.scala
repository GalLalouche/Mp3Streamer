package backend.pkg

import common.MyScalaModule

private object PkgModule extends MyScalaModule {
  override def configure(): Unit = {
    install[ZipperFactory]
  }
}
