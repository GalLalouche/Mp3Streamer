package backend.lyrics

import models.Song

import common.storage.Storage

trait LyricsStorage extends Storage[Song, Lyrics]
