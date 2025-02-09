// A very simple module, showing the most recent album, so it can be easily added to the playlist.

export namespace LastAlbum {
  export async function addNextNewAlbum(): Promise<void> {
    return $.get("recent/last", album => updateAlbum(album, true)).toPromise()
  }
}

import {Album, gplaylist} from "./types.js"

const ADD = "plus"

function write(value: JQuery<HTMLElement>): void {
  $("#last_album").empty().append(value)
}

function albumText(album: Album): string {
  return `${icon(ADD)} ${album.artistName}: ${album.title}`
}

function addAlbum(album: Album): void {
  $.get("data/albums/" + album.dir, e => gplaylist.add(e, false))
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
  write(span("Fetching last album..."))
  $.get("recent/last", album => updateAlbum(album, false))
  $exposeGlobally!(LastAlbum)
})
