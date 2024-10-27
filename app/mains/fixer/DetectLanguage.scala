package mains.fixer

import scala.util.Try

private object DetectLanguage {
  def apply(s: String): Try[String] = Try(languageDetector.detect(s))

  private lazy val languageDetector = PythonLanguageDetector.create()
}
