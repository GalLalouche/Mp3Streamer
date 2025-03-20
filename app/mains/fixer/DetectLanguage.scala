package mains.fixer

import java.io.IOException

import com.google.inject.{Inject, Singleton}

import scala.concurrent.duration.Duration

@Singleton class DetectLanguage @Inject() private (@DetectLanguageTimeout timeout: Duration) {
  def apply(s: String): Either[IOException, String] = languageDetector.detect(s)

  private lazy val languageDetector = PythonLanguageDetector.create(timeout)
}
