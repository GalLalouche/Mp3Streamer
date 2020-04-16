package backend.lyrics.retrievers

import backend.lyrics.Instrumental

private class DefaultInstrumentalHelper(defaultType: String) {
  def instrumental: Instrumental = Instrumental(s"Default for $defaultType")
  def apply(isInstrumental: Boolean): RetrievedLyricsResult =
    if (isInstrumental) RetrievedLyricsResult.RetrievedLyrics(instrumental)
    else RetrievedLyricsResult.NoLyrics
}
