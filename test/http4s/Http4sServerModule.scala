package http4s

import net.codingwell.scalaguice.ScalaModule
import server.Server

private object Http4sServerModule extends ScalaModule {
  override def configure(): Unit = {
    install(Http4sModule)
    bind[Server].to[Http4sServer]
  }
}
