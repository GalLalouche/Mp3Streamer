// Since jplayer.playlist.js is too freaking big, this extracts my customization

$(function () {
  const playlistElement = $(".jp-playlist")
  const playlist = gplaylist
  const playlistItem = "> ul > li"

  playlistElement.on("mouseover", playlistItem, function () {
    const listItem = $(this)
    // The listItem can't overflow; what can overflow is the width-limited descendent.
    if (listItem.find(".width-limited-playlist-span").custom_overflown()) {
      const displayedIndex = playlist.getDisplayedIndex(listItem.index())
      const song = playlist.songs()[displayedIndex]
      listItem.custom_tooltip(playlist.toString(song))
    }
  })
  // Move to song on click.
  playlistElement.on("click", playlistItem, function (e) {
    if (e.target.localName !== "span" && e.target.localName !== "img")
      return // Only listens to clicks on the text or poster image, to avoid handling misclicks near the buttons.
    const listItem = $(this)
    const clickedIndex = playlist.getDisplayedIndex(listItem.index())
    if (gplaylist.currentIndex() === clickedIndex)
      return // Clicked song is currently playing.
    playlist.play(clickedIndex)
  })
})

class PlaylistCustomizations {
  private static isClassicalPiece(song: Song): boolean { return !!song.composer}

  private static additionalData(song: Song): string[] {
    if (PlaylistCustomizations.isClassicalPiece(song).isFalse())
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

  static formattedMetadata(song: Song): string {
    const res = PlaylistCustomizations.additionalData(song)
    const head = `<span dir="ltr">${res[0]}</span>`
    res.shift()
    res.push(song.bitrate + "kbps")
    return `${head}, <span dir="ltr">${res.join(", ")}</span>`
  }


  static mediaMetadata(song: Song): string {
    return [
      song.title,
      song.artistName,
    ].concat(PlaylistCustomizations.additionalData(song)).concat([
      song.duration.timeFormat(),
      song.bitrate + "kbps",
    ]).join(", ")
  }
}

// This is meant to be monkey-patched into playlist (hence the use of this.options).
function mediaMetadataHtml(this: { options: any }, song: Song): string {
  const metadata =
    `<span class="jp-artist" dir="ltr">${song.artistName}</span> ` +
    `(<span class="jp-parens">${PlaylistCustomizations.formattedMetadata(song)}</span>`

  // Duration is appended manually outside of metadata to ensure that it is always displayed, even
  // if metadata overflows. That's the reason for the odd parens too.
  return (
    `<span class='${this.options.playlistOptions.itemClass}' tabindex='1'>
          <span class="width-limited-playlist-span">
            <span class="jp-title">${song.title}</span> <span class="jp-metadata">${metadata}</span>
          </span><!--
          --><span class="jp-list-duration">, ${song.duration.timeFormat()})</span>
        </span>`
  )
}

