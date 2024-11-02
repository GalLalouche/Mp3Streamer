// A very simple module, showing the most recent album, so it can be easily added to the playlist.

export namespace LastAlbum {
  export function reopenLastAlbumWebsocketIfNeeded(): void {
    if (
      last_album_websocket === undefined ||
      last_album_websocket.readyState === last_album_websocket.CLOSED
    )
      openLastAlbumConnection()
  }

  export function addNextNewAlbum(): void {shouldAddNextNewAlbum = true}
}

import openConnection from "./ws_common.js"
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

let shouldAddNextNewAlbum: boolean = false
let lastAlbumText: string | undefined = undefined

function updateAlbum(album: Album): void {
  const text = albumText(album)
  if (text !== lastAlbumText && shouldAddNextNewAlbum) {
    addAlbum(album)
    shouldAddNextNewAlbum = false
  }
  console.log(`Updating last album: '${text}'`)
  const albumElement = elem("span", text)
  write(albumElement)
  albumElement.find(".fa-" + ADD).click(() => addAlbum(album))
  if (albumElement.custom_overflown())
    albumElement.custom_tooltip(text)
  lastAlbumText = text
}

let last_album_websocket: WebSocket | undefined = undefined

function openLastAlbumConnection(): void {
  last_album_websocket = openConnection("last_album", msg => updateAlbum(JSON.parse(msg.data)))
}

$(function () {
  write(span("Fetching last album..."))
  LastAlbum.reopenLastAlbumWebsocketIfNeeded()
  $exposeGlobally!(LastAlbum)
})
