package backend.lyrics.retrievers

import backend.lyrics.{Instrumental, LyricsUrl}

private class DefaultInstrumentalHelper(defaultType: String) {
  def instrumental: Instrumental = Instrumental(s"Default for $defaultType", LyricsUrl.DefaultEmpty)
  def apply(isInstrumental: Boolean): RetrievedLyricsResult =
    if (isInstrumental) RetrievedLyricsResult.RetrievedLyrics(instrumental)
    else RetrievedLyricsResult.NoLyrics
}
