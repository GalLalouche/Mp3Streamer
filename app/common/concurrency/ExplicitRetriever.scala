package common.concurrency

import backend.Retriever

import scala.concurrent.ExecutionContext

trait ExplicitRetriever[A, B] {
  def withExecutionContext(ec: ExecutionContext): Retriever[A, B]
}
