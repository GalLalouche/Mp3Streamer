// A very simple module, showing the latest albums, so it can be easily added to the playlist.

import {Album, gplaylist} from "./types.js"

export namespace LastAlbum {
  export async function addNextNewAlbum(): Promise<void> {
    if (nonEmptyQueue) { // If the queue isn't empty, dequeue first for faster update time.
      await dequeue()
      await updateLatestAlbum()
    } else {
      await updateLatestAlbum()
      await dequeue()
    }
  }

  export async function updateLatestAlbum(): Promise<void> {
    setFetchingText()
    updateAlbums(await forceUpdate())
  }
}

function setFetchingText(): void {
  write("Fetching last albums...")
}

const prefix = "last_albums"

async function getLastAlbums(): Promise<void> {
  setFetchingText()
  updateAlbums(await $.get(prefix).toPromise())
}

async function forceUpdate(): Promise<Album[]> {
  return $.post(prefix + "/update").toPromise()
}

const ADD = "plus"

function write(text: string): JQuery<HTMLElement> {
  return $("#last_album").empty().append(span(text))
}

let nonEmptyQueue: boolean = false

function updateAlbums(albums: Album[]): void {
  if (albums.length == 0) {
    write("No new albums")
    return
  }
  const firstAlbum = albums[0]
  nonEmptyQueue = true
  const extra = albums.length > 1 ? ` (+ ${albums.length - 1} more)` : ""
  const formatAlbum = (album: Album) => `${album.artistName}: ${album.title}`
  const text = `${icon(ADD)} ${formatAlbum(firstAlbum)}${extra}`
  console.log(`Updating last album: '${text}'`)
  const albumElement = write(text)
  albumElement.find(".fa-" + ADD).on("click", (() => dequeue()))
  if (albumElement.custom_overflown() || extra.length > 0) {
    const formatted = albums.map(formatAlbum)
    const extraOverflowText = albums.length > 1 ? ` (+ ${formatted.slice(1).join(", ")})` : ""
    albumElement.custom_tooltip(`${formatted[0]}${extraOverflowText}`)
  }
}

async function dequeue(): Promise<void> {
  const [head, tail] = await $.post(prefix + "/dequeue").toPromise()
  await $.get("data/album/" + head.dir, e => gplaylist.add(e, false)).toPromise()
  nonEmptyQueue = tail.length > 0
  return LastAlbum.updateLatestAlbum()
}

$(function () {
  // noinspection JSIgnoredPromiseFromCall
  getLastAlbums()
})
