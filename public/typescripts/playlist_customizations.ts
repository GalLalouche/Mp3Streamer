// Since jplayer.playlist.js is too freaking big, this extracts (some of) my customization.
// FIXME merge this and the original playlist, rewrite the whole thing in typescript.

import {match, P} from "ts-pattern";
import * as External from "./external.js"
import {GuiEvents, PlaylistEventTopic} from "./gui_events.js";
import {ITEM_CLASS} from "./jplayer.playlist.js";
import {Song} from "./media.js";
import {gplaylist} from "./player_singleton.js";
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

function isClassicalPiece(song: Song): boolean { return !!song.composer}

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

  playlistElement.on("mouseover", playlistItem, function () {
    const listItem = $(this)
    // The listItem can't overflow; what can overflow is the width-limited descendent.
    if (listItem.find(".width-limited-playlist-span").custom_overflown()) {
      const displayedIndex = gplaylist.getDisplayedIndex(listItem.index())
      const song = gplaylist.songs()[displayedIndex]
      listItem.custom_tooltip(mediaMetadataHtml(song))
    }
  })
  // Move to song on click.
  // FIXME this doesn't work on Chrome for some reason. It always registers the click as on the div.
  GuiEvents.listen(PlaylistEventTopic, async event => {
    match(event)
      .with('next', () => gplaylist.next())
      .with('previous', () => gplaylist.prev())
      .with({type: P.select("type"), index: P.select("index")}, ({type, index}) => {
        match(type)
          .with("select", () => gplaylist.select(index))
          .with(P.select(), x => gplaylist.removeItem(index, x))
          .exhaustive()
      })
  })

  $("body").append(String.raw`
    <ul id="contextMenu" class="ui-menu" style="display:none;">
        <li><div><span class="menu-icon fa fa-arrows-v"/></span> Score</div></li>
        <li><div><span class="menu-icon fa fa-refresh"></span> Refresh</div></li>
        <style>
        .ui-menu {
            width: 150px;
            background-color: white;
            border: 1px solid #ccc;
            box-shadow: 2px 2px 5px rgba(0,0,0,0.2);
        }
        .menu-icon {
            margin-right: 5px;
            width: 15px;
            text-align: center;
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

    const song = gplaylist.songs()[gplaylist.getDisplayedIndex($(this).index())]
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
