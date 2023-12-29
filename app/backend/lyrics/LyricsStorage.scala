package backend.lyrics

import models.Song

import common.storage.Storage

private trait LyricsStorage extends Storage[Song, Lyrics]
