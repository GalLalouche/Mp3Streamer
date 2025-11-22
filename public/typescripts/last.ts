// A very simple module, showing the latest albums, so it can be easily added to the playlist.

import {Album, gplaylist} from "./types.js"

export namespace LastAlbum {
  export async function addNextNewAlbum(): Promise<void> {
    await updateLatestAlbum()
    return dequeue()
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

function updateAlbums(albums: Album[]): void {
  if (albums.length == 0) {
    write("No new albums")
    return
  }
  const album = albums[0]
  const extra = albums.length > 1 ? ` (+ ${albums.length - 1} more)` : ""
  const text = `${icon(ADD)} ${album.artistName}: ${album.title}${extra}`
  console.log(`Updating last album: '${text}'`)
  const albumElement = write(text)
  albumElement.find(".fa-" + ADD).on("click", (() => dequeue()))
  if (albumElement.custom_overflown())
    albumElement.custom_tooltip(text)
}

async function dequeue(): Promise<void> {
  const [head, tail] = await $.post(prefix + "/dequeue").toPromise()
  await $.get("data/album/" + head.dir, e => gplaylist.add(e, false)).toPromise()
  updateAlbums(tail)
}

$(function () {
  // noinspection JSIgnoredPromiseFromCall
  getLastAlbums()
})
