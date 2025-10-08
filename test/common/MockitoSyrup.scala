package common

import org.mockito.{ArgumentCaptor, Mockito}
import org.mockito.Mockito.atLeast

import scala.jdk.CollectionConverters.ListHasAsScala

import common.rich.primitives.RichClass._

/** More mockito sugar. */
object MockitoSyrup {
  def captor[A: Manifest]: ArgumentCaptor[A] = ArgumentCaptor.forClass(manifest.unerasedClass)
  class Capturer[A] private[MockitoSyrup] (a: A) {
    def apply[B: Manifest](f: (A, B) => Any): collection.Seq[B] = {
      val c = captor[B]
      f(Mockito.verify(a, atLeast(0)), c.capture())
      c.getAllValues.asScala
    }
  }
  def captureAll[A](a: A): Capturer[A] = new Capturer[A](a)
}
