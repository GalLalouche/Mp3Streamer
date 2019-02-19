package mains.vimtag

import models.RichTag._
import org.jaudiotagger.tag.{FieldKey, Tag}

private sealed trait ParsedTag[+A] {
  def get(fieldKey: FieldKey, originalTag: Tag): Option[String]
  def map[B](f: A => B): ParsedTag[B]
}
private object Empty extends ParsedTag[Nothing] {
  override def get(fieldKey: FieldKey, originalTag: Tag) = None
  override def map[B](f: Nothing => B) = this
}
private trait RequiredTag[+A] extends ParsedTag[A] {
  override def map[B](f: A => B): RequiredTag[B]
}
private object RequiredTag {
  def apply(s: String): RequiredTag[String] = s match {
    case Tags.Keep => Keep
    case Tags.ExplicitEmpty => ExplicitEmpty
    case _ => Change(s)
  }
}
private object ExplicitEmpty extends RequiredTag[Nothing] {
  override def get(fieldKey: FieldKey, originalTag: Tag) = None
  def map[B](f: Nothing => B) = this
}
private object Keep extends RequiredTag[Nothing] {
  override def get(fieldKey: FieldKey, originalTag: Tag) = originalTag.firstNonEmpty(fieldKey)
  def map[B](f: Nothing => B) = this
}
private case class Change[+A](a: A) extends RequiredTag[A] {
  override def get(fieldKey: FieldKey, originalTag: Tag) = Some(a.toString)
  def map[B](f: A => B) = Change(f(a))
}
