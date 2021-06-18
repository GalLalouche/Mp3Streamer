package mains.vimtag

private object Common {
  def dummyInitialValuesMap: Map[String, InitialValues] =
    Map.empty[String, InitialValues].withDefaultValue(new InitialValues(Nil))

  def dummyWithOpus: Map[String, InitialValues] = {
    val initialOpusValues = Vector(
      "Op. 14",
      "Op. 15",
      "Op. 14",
    )
    dummyInitialValuesMap + ("OPUS" -> new InitialValues(initialOpusValues))
  }
}
