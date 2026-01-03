package common

import _root_.rx.lang.scala.{Observable, Observer, Subject}

/** Listens to A, emits B. */
trait PipeSubject[A, B] extends Observer[A] with Observable[B]
object PipeSubject {
  private class PipeSubjectImpl[A, B](f: A => B) extends PipeSubject[A, B] {
    private val sb: Subject[B] = Subject[B]()

    override val asJavaObservable: _root_.rx.Observable[_ <: B] = sb.asJavaObservable
    override def onNext(value: A) = sb.onNext(f(value))
    override def onError(error: Throwable) = sb.onError(error)
    override def onCompleted() = sb.onCompleted()
  }

  def apply[A, B](f: A => B): PipeSubject[A, B] = new PipeSubjectImpl(f)
}
