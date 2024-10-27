package mains.cleaner.stringfixer

import java.io.File

import backend.module.StandaloneModule
import com.google.inject.Guice
import net.codingwell.scalaguice.InjectorExtensions.ScalaInjector

import scala.sys.process._

private object StringFixerValidator {
  def foo[T: Manifest] =
    println(manifest.runtimeClass)
  def main(args: Array[String]): Unit =
    println(
      Guice
        .createInjector(StandaloneModule)
        .instance[TitleValidator]
        .go()
        .take(1)
        // .groupBy(_.song.file.parent)
        // .mapValues(_.map(_.formattedString) mkString "\n")
        .foreach(fix),
    )
  // private sealed trait Field extends EnumEntry {
  //  def extract: Song => String
  // }
  // private object Field extends Enum[Field] {
  //  case object Title extends Field {
  //    override def extract = _.title
  //  }
  //  override def values = findValues
  // }
  // private case class Difference(file: FileRef, field: Field, actual: String, expected: String) {
  //  override def toString = s"$file @ $field,\n\tactual  : $actual\n\texpected: $expected"
  // }
  private def fix(d: DirectoryAction): Unit = {
    d.go()
    assert(
      Vector(
        "C:\\Users\\Gal\\AppData\\Local\\Programs\\Python\\Python310\\python.exe",
        new File(".").getAbsolutePath + "/scripts/python/reload_directory.py",
        d.directory.path,
      ).! == 0,
    )
  }
}
