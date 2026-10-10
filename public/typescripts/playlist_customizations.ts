// Since jplayer.playlist.js is too freaking big, this extracts (some of) my customization.
// FIXME merge this and the original playlist, rewrite the whole thing in typescript.
//  Actually, this is only used for the metadata HTML? That's not a half-bad cohesive module. Should
//  probably be renamed though.

import {ITEM_CLASS} from "./gui_events.js"
import {Song} from "./media.js"

export function mediaMetadataHtml(song: Song): string {
  const metadata =
    `<span class="jp-artist" dir="ltr">${song.artistName}</span> ` +
    `(<span class="jp-parens">${formattedMetadata(song)}</span>`

  // Duration is appended manually outside of metadata to ensure that it is always displayed, even
  // if metadata overflows. That's the reason for the odd parens too.
  // FIXME this ITEM_CLASS shouldn't be here. It's enough that it's on the list item.
  return (
    `<span class='${ITEM_CLASS}' tabindex='1'>
       <span class="width-limited-playlist-span">
         <span class="jp-title">${song.title}</span> <span class="jp-metadata">${metadata}</span>
       </span><!-- Avoids the whitespace between the two spans.
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

function isClassicalPiece(song: Song): boolean {return isNonNullable(song.composer)}

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
