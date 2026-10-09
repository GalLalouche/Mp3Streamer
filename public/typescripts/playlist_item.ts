import {Song} from "./media"
import {mediaMetadataHtml} from "./playlist_customizations"

import * as ColorUtils from "./color-utils.js"

export class PlaylistItem {
  readonly song: Song
  readonly element: JQuery<HTMLElement>

  static create(song: Song): PlaylistItem {
    return new PlaylistItem(song, createElement(song))
  }
  private constructor(song: Song, element: JQuery<HTMLElement>) {
    this.song = song
    this.element = element
  }
}

function createElement(song: Song): JQuery<HTMLElement> {
  let listItem = "<li><div>" + mediaMetadataHtml(song)

  function appendIcon(clazz: string, char: string) {
    listItem += `<a href='javascript:;' class='${REMOVE_ITEM} ${clazz}'>${char}</a>`
  }

  appendIcon(REMOVE_THIS, "&times;")
  appendIcon(REMOVE_UP, "&uparrow;")
  appendIcon(REMOVE_DOWN, "&downarrow;")
  listItem += "</div></li>"

  const result = $(listItem)
  result.prepend(img(song.poster).addClass("playlist-item-poster"))
  ColorUtils.getColor(song.poster).then(rgb => {
    result.css('background-color', rgb.makeLighter(0.1).toString())
  })
  return result
}

const REMOVE_ITEM = "jp-playlist-item-remove"
const REMOVE_THIS = "jp-playlist-item-remove-this"
const REMOVE_UP = "jp-playlist-item-remove-up"
const REMOVE_DOWN = "jp-playlist-item-remove-down"

