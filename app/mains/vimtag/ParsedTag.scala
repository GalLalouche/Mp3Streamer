package mains.vimtag

import org.jaudiotagger.tag.{FieldKey, Tag}

import common.RichTag._

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
  def parser(iv: InitialValues): String => RequiredTag[String] = s =>
    s.toUpperCase match {
      case Tags.Keep => Keep
      case Tags.ExplicitEmpty => ExplicitEmpty
      case Tags.Common => Change(iv.mostCommon)
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
