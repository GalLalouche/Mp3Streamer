import {Song} from "./media"
import {mediaMetadataHtml} from "./playlist_customizations"
import * as ColorUtils from "./color-utils.js"
import {ITEM_CLASS, REMOVE_DOWN, REMOVE_ITEM, REMOVE_THIS, REMOVE_UP} from "./gui_events"

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

$(function () {
  const playlistElement = $(".jp-playlist")
  const playlistItem = "> ul > li"

  // Mouseover tooltip for overflowing playlist items.
  playlistElement.on("mouseover", playlistItem, function () {
    const listItem = $(this)
    // The listItem can't overflow; what can overflow is the width-limited descendent.
    if (listItem.find(".width-limited-playlist-span").custom_overflown()) {
      const song = listItem.data("song") as Song
      listItem.custom_tooltip(mediaMetadataHtml(song))
    }
  })
})
