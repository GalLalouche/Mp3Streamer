import {Song} from "./media.js"
import {mediaMetadataHtml} from "./media_metadata.js"
import * as ColorUtils from "./color-utils.js"
import {ITEM_CLASS, REMOVE_DOWN, REMOVE_ITEM, REMOVE_THIS, REMOVE_UP} from "./event_setup.js"
import {GuiEvents, PlaylistContextMenuEventTopic} from "./events.js"

export type RemoveType = "x" | "up" | "down"
export type PlaylistItemClickType = "select" | RemoveType

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

  listen(listener: (arg: PlaylistItemClickType) => void): void {
    this.element.on("click", function (e) {
      const target = $(e.target)

      function getRemoveType(target: JQuery<HTMLElement>): RemoveType {
        if (target.hasClass(REMOVE_THIS)) {
          return "x"
        } else if (target.hasClass(REMOVE_UP)) {
          return "up"
        } else if (target.hasClass(REMOVE_DOWN)) {
          return "down"
        } else {
          throw new Error("Unknown remove type: " + target.attr("class"))
        }
      }

      const removeButton = target.closest("a." + REMOVE_ITEM)
      if (removeButton.length > 0)
        listener(getRemoveType(removeButton))
      // ignore clicks between the remove button
      else if (target.closest("." + REMOVE_ITEM).length === 0)
        listener("select")
    })
  }
}

function createElement(song: Song): JQuery<HTMLElement> {
  let listItem = "<li><div>" + mediaMetadataHtml(song)

  listItem += `<span class="${REMOVE_ITEM} jp-playlist-item-actions">`
  function appendIcon(clazz: string, char: string) {
    listItem += `<a href='javascript:;' class='${REMOVE_ITEM} ${clazz}'>${char}</a>`
  }

  appendIcon(REMOVE_THIS, "&times;")
  appendIcon(REMOVE_UP, "&uparrow;")
  appendIcon(REMOVE_DOWN, "&downarrow;")
  listItem += "</span></div></li>"

  const result = $(listItem)
  result.prepend(img(song.poster).addClass("playlist-item-poster"))
  ColorUtils.getColor(song.poster).then(rgb => {
    result.css('background-color', rgb.makeLighter(0.1).toString())
  })
  result.addClass(ITEM_CLASS)

  result.on("mouseover", function () {
    // The listItem can't overflow; what can overflow is the width-limited descendent.
    if (result.find(".width-limited-playlist-span").custom_overflown())
      result.custom_tooltip(mediaMetadataHtml(song))
  })

  result.on("contextmenu", function (e) {
    e.preventDefault() // Prevent the default context menu

    contextMenu.css({
      display: 'block',
      top: e.pageY + 5,
      left: e.pageX + 5,
      position: 'absolute',
    })

    contextMenu.one("click", "li", async function (e) {
      function publish(type: "score" | "external") {
        GuiEvents.publish(PlaylistContextMenuEventTopic, {type: type, song: song})
      }
      switch (e.target.textContent.trim()) {
        case "Score":
          publish("score")
        case "Refresh":
          publish("external")
        default:
          throw new AssertionError("Unexpected selection: " + e.target.textContent)
      }
    })

    $(document).one("click", () => $("#contextMenu").hide())
  })

  return result
}

let contextMenu!: JQuery<HTMLElement>

$(function () {
  $("body").append(String.raw`
    <ul id="contextMenu" class="ui-menu" style="display:none;">
        <li><div><span class="menu-icon fa fa-arrows-v"/></span> Score</div></li>
        <li><div><span class="menu-icon fa fa-refresh"></span> Refresh</div></li>
        <style>
        .ui-menu {
            width: 150px
            background-color: white
            border: 1px solid #ccc
            box-shadow: 2px 2px 5px rgba(0,0,0,0.2)
        }
        .menu-icon {
            margin-right: 5px
            width: 15px
            text-align: center
        }
        </style>
    </ul>
  `)
  contextMenu = $("#contextMenu").menu()
})
