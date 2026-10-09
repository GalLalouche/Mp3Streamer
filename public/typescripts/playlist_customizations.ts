// Since jplayer.playlist.js is too freaking big, this extracts (some of) my customization.
// FIXME merge this and the original playlist, rewrite the whole thing in typescript.
//  Actually, this is only used for the metadata HTML? That's not a half-bad cohesive module. Should
//  probably be renamed though.

import * as External from "./external.js"
import {ITEM_CLASS} from "./playlist_item.js" // FIXME temporary cyclic
import {Song} from "./media.js"
import * as Score from "./score.js"

export function mediaMetadataHtml(song: Song): string {
  const metadata =
    `<span class="jp-artist" dir="ltr">${song.artistName}</span> ` +
    `(<span class="jp-parens">${formattedMetadata(song)}</span>`

  // Duration is appended manually outside of metadata to ensure that it is always displayed, even
  // if metadata overflows. That's the reason for the odd parens too.
  return (
    `<span class='${ITEM_CLASS}' tabindex='1'>
          <span class="width-limited-playlist-span">
            <span class="jp-title">${song.title}</span> <span class="jp-metadata">${metadata}</span>
          </span><!--
          --><span class="jp-list-duration">, ${song.duration.timeFormat()})</span>
        </span>`
  )
}


function formattedMetadata(song: Song): string {
  const res = additionalData(song)
  const head = `<span dir="ltr">${res[0]}</span>`
  res.shift()
  res.push(song.bitrate + "kbps")
  return `${head}, <span dir="ltr">${res.join(", ")}</span>`
}

function isClassicalPiece(song: Song): boolean {return !!song.composer}

function additionalData(song: Song): string[] {
  if (isClassicalPiece(song).isFalse())
    return [
      `${song.albumName}${song.discNumber ? "[" + song.discNumber + "]" : ""}`,
      song.track.toString(),
      song.year.toString(),
    ]

  const titleContainsComposer =
    song.albumName.toLowerCase().includes(song.composer?.toLowerCase()!)
  const pieceTitle = titleContainsComposer ? song.albumName : `${song.composer}'s ${song.albumName}`
  const opusSuffix = song.opus ? `, ${song.opus}` : ''
  return [
    pieceTitle + opusSuffix,
    song.year,
    song.conductor,
    song.orchestra,
    song.performanceYear,
    song.track,
  ].filter(x => x)
    .map(x => x!.toString())
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

  // Context menu for playlist items.
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
  const contextMenu = $("#contextMenu").menu()
  playlistElement.on("contextmenu", playlistItem, function (e) {
    e.preventDefault() // Prevent the default context menu

    contextMenu.css({
      display: 'block',
      top: e.pageY + 5,
      left: e.pageX + 5,
      position: 'absolute',
    })

    const song = $(this).closest('.' + ITEM_CLASS).data("song") as Song
    contextMenu.one("click", "li", async function (e) {
      switch (e.target.textContent.trim()) {
        case "Score":
          return Score.popup(song)
        case "Refresh":
          return External.refreshRemote(song)
        default:
          throw new AssertionError("Unexpected selection: " + e.target.textContent)
      }
    })

    $(document).one("click", () => $("#contextMenu").hide())
  })
})
