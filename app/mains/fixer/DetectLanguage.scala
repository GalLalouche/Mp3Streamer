package mains.fixer

import scala.sys.process.Process

import common.rich.RichT.richT
import org.apache.tika.langdetect.optimaize.OptimaizeLangDetector

private object DetectLanguage {
  def apply(s: String): String = javaDetection(s).getOrElse(pythonDetection(s))

  private def javaDetection(s: String): Option[String] =
    detector.detect(s).getLanguage.optFilter(_.nonEmpty)
  private lazy val detector = new OptimaizeLangDetector().loadModels()

  // Honestly, this is more robust than the tika nonsense, but it's probably slower to apply it
  // en-masse to a huge number of songs, e.g., for indexing. So we apply this if the tika code
  // fails.
  // TODO One could create a python executable waiting for input I suppose and keep it alive for the
  //  remainder of the program...
  private def pythonDetection(s: String): String =
    // To install, use 'pip install langdetect'
    Process(Vector("python", "-c", s"from langdetect import detect; print(detect('$s'))")).!!.trim
}
