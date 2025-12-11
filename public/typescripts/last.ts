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
  const firstAlbum = albums[0]
  const extra = albums.length > 1 ? ` (+ ${albums.length - 1} more)` : ""
  const formatAlbum = (album: Album) => `${album.artistName}: ${album.title}`
  const text = `${icon(ADD)} ${formatAlbum(firstAlbum)}${extra}`
  console.log(`Updating last album: '${text}'`)
  const albumElement = write(text)
  albumElement.find(".fa-" + ADD).on("click", (() => dequeue()))
  if (albumElement.custom_overflown() || extra.length > 0) {
    var formatted = albums.map(formatAlbum)
    const extraOverflowText = albums.length > 1 ? ` (+ ${formatted.slice(1).join(", ")})` : ""
    albumElement.custom_tooltip(`${formatted[0]}${extraOverflowText}`)
  }
}

async function dequeue(): Promise<void> {
  const [head, _] = await $.post(prefix + "/dequeue").toPromise()
  await $.get("data/album/" + head.dir, e => gplaylist.add(e, false)).toPromise()
  return LastAlbum.updateLatestAlbum()
}

$(function () {
  // noinspection JSIgnoredPromiseFromCall
  getLastAlbums()
})
