package common.io

import play.api.libs.ws.{StandaloneWSClient, StandaloneWSRequest, StandaloneWSResponse}

object WSAliases {
  type WSClient = StandaloneWSClient
  type WSRequest = StandaloneWSRequest
  type WSResponse = StandaloneWSResponse
}
