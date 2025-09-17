// A very simple module, showing the most recent album, so it can be easily added to the playlist.

import {Album, gplaylist} from "./types.js"
import {Try, tryF} from "./try.js"

export namespace LastAlbum {
  export async function addNextNewAlbum(): Promise<void> {
    const last = await updateLastAlbum()
    const lastText = albumText(last)
    if (lastText != lastAlbumText)
      updateAlbum(last, true)
  }

  export async function updateLatestAlbum(): Promise<void> {
    write(span("Fetching last album..."))
    updateLastAlbum().then(album => updateAlbum(album, false))
  }
}

async function getLastAlbum(): Promise<Album> {
  const last: Try<Album> = await tryF($.get("recent/get_last").toPromise())
  return last == null || last instanceof Error ? forceUpdate() : last
}

async function updateLastAlbum(): Promise<Album> {
  const last = await getLastAlbum()
  const getLastText = albumText(last)
  return getLastText != lastAlbumText ? last : forceUpdate()
}

async function forceUpdate(): Promise<Album> {
  return $.get("recent/update_last").toPromise()
}

const ADD = "plus"

function write(value: JQuery<HTMLElement>): void {
  $("#last_album").empty().append(value)
}

function albumText(album: Album): string {
  return `${icon(ADD)} ${album.artistName}: ${album.title}`
}

function addAlbum(album: Album): void {
  $.get("data/album/" + album.dir, e => gplaylist.add(e, false))
}

let lastAlbumText: string | undefined = undefined

function updateAlbum(album: Album, addToPlaylist: boolean): void {
  const text = albumText(album)
  if (addToPlaylist && text !== lastAlbumText)
    addAlbum(album)
  console.log(`Updating last album: '${text}'`)
  const albumElement = elem("span", text)
  write(albumElement)
  albumElement.find(".fa-" + ADD).click(() => addAlbum(album))
  if (albumElement.custom_overflown())
    albumElement.custom_tooltip(text)
  lastAlbumText = text
}

$(function () {
  // noinspection JSIgnoredPromiseFromCall
  LastAlbum.updateLatestAlbum()
})
