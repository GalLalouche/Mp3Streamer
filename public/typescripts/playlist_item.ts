import {Song} from "./media"
import {mediaMetadataHtml} from "./playlist_customizations"
import * as ColorUtils from "./color-utils.js"
import {REMOVE_DOWN, REMOVE_ITEM, REMOVE_THIS, REMOVE_UP} from "./gui_events"

export const ITEM_CLASS = "jp-playlist-item"

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
  result.data("song", song)
  result.addClass(ITEM_CLASS)
  return result
}

