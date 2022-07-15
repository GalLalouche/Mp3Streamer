package songs.selector

import com.google.inject.Provider
import javax.inject.{Inject, Singleton}
import models.Song

import scala.concurrent.Future

import common.concurrency.Extra

@Singleton
private class EagerSongSelectorState @Inject()(songSelectorProvider: Provider[SongSelector]) extends SongSelector {
  private var state: SongSelector = songSelectorProvider.get()
  private val extra = Extra("SongSelectorState") {
    state = songSelectorProvider.get()
  }
  def update(): Future[Unit] = extra.!()
  override def randomSong(): Song = state.randomSong()
}
