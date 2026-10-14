import {Song} from "./media.js"

export function mediaMetadataHtml(song: Song): string {
  const metadata =
    `<span class="jp-artist" dir="ltr">${song.artistName}</span> ` +
    `(<span class="jp-parens">${formattedMetadata(song)}</span>`

  // Duration is appended manually outside of metadata to ensure that it is always displayed, even
  // if metadata overflows. That's the reason for the odd parens too.
  return (
    `<span tabindex='1'>
       <span class="width-limited-playlist-span">
         <span class="jp-title">${song.title}</span> <span class="jp-artist-by">by </span><span class="jp-metadata">${metadata}</span>
       </span><!-- Avoids the whitespace between the two spans.
       --><span class="jp-list-duration">, ${song.duration.timeFormat()})</span>
     </span>`
  )
}

export function mediaMetadata(song: Song): string {
  return [
    song.title,
    song.artistName,
  ].concat(additionalData(song)).concat([
    song.duration.timeFormat(),
    song.bitrate + "kbps",
  ]).join(", ")
}

function formattedMetadata(song: Song): string {
  const res = additionalData(song)
  const head = `<span dir="ltr">${res[0]}</span>`
  res.shift()
  res.push(song.bitrate + "kbps")
  return `${head}, <span dir="ltr">${res.join(", ")}</span>`
}

function additionalData(song: Song): string[] {
  const isClassicalPiece = isNonNullable(song.composer)
  if (isClassicalPiece.isFalse())
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
