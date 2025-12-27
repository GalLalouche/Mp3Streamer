package common.io

import com.google.inject.{Inject, Provider}

import scala.concurrent.ExecutionContext

import common.io.WSAliases._

/**
 * For when you need to talk to the internet with a specific [[ExecutionContext]]. Note that the
 * first HTTP request made will use a different context (AsyncHttpClient's internal one) so this is
 * only applicable for any subsequent [[scala.util.concurrent.Future]] applications.
 */
class InternetTalkerFactory @Inject() (wsClientProver: Provider[WSClient]) {
  def withExecutionContext(ec: ExecutionContext): InternetTalker =
    new InternetTalker(wsClientProver, ec)
}
