package mains.fixer

private object DetectLanguage {
  def apply(s: String): String = languageDetector.detect(s)

  private lazy val languageDetector = PythonLanguageDetector.create()
}
