// A very simple module, showing the most recent album, so it can be easily added to the playlist.

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

export class LastAlbum {
  private static shouldAddNextNewAlbum: boolean = false
  private static lastAlbumText?: string = undefined

  private static updateAlbum(album: Album): void {
    const text = albumText(album)
    if (text !== this.lastAlbumText && this.shouldAddNextNewAlbum) {
      addAlbum(album)
      this.shouldAddNextNewAlbum = false
    }
    console.log(`Updating last album: '${text}'`)
    const albumElement = elem("span", text)
    write(albumElement)
    albumElement.find(".fa-" + ADD).click(() => addAlbum(album))
    if (albumElement.custom_overflown())
      albumElement.custom_tooltip(text)
    this.lastAlbumText = text
  }

  private static last_album_websocket?: WebSocket = undefined

  private static openLastAlbumConnection(): void {
    this.last_album_websocket =
      openConnection("last_album", msg => this.updateAlbum(JSON.parse(msg.data)))
  }

  static reopenLastAlbumWebsocketIfNeeded(): void {
    if (this.last_album_websocket === undefined ||
      this.last_album_websocket.readyState === this.last_album_websocket.CLOSED)
      this.openLastAlbumConnection()
  }

  static addNextNewAlbum(): void {this.shouldAddNextNewAlbum = true}
}

$(function () {
  write(span("Fetching last album..."))
  LastAlbum.reopenLastAlbumWebsocketIfNeeded();
  (window as any).LastAlbum = LastAlbum
})
