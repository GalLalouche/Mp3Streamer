package mains.fixer

import common.rich.RichT.richT
import org.apache.tika.langdetect.optimaize.OptimaizeLangDetector

private object DetectLanguage {
  def apply(s: String): String = javaDetection(s).getOrElse(pythonDetection(s))

  private def javaDetection(s: String): Option[String] =
    detector.detect(s).getLanguage.optFilter(_.nonEmpty)
  private lazy val detector = new OptimaizeLangDetector().loadModels()

  private def pythonDetection(s: String): String = languageDetector.detect(s)
  private lazy val languageDetector = PythonLanguageDetector.create()
}
