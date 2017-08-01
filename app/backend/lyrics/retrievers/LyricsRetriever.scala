package backend.lyrics.retrievers

import backend.Retriever
import backend.lyrics.Lyrics
import models.Song

private[lyrics] trait LyricsRetriever extends Retriever[Song, Lyrics]
