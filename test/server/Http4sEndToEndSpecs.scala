package server

import http4s.Http4sServerModule

private abstract class Http4sEndToEndSpecs extends EndToEndTest {
  protected override def serverModule = Http4sServerModule
}
