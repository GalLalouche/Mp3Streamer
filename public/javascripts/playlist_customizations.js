// Since jplayer.playlist.js is too freaking big, this extracts my customization
$(function() {
  const playlistElement = $(".jp-playlist")
  const playlist = gplaylist
  const playlistItem = "ul li"
  // Display full song info on hover when the text overflows.
  playlistElement.on("mouseover", playlistItem, function() {
    const listItem = $(this)
    // The listItem can't overflow; what can overflow the width-limited descendent.
    if (listItem.find(".width-limited-playlist-span").custom_overflown()) {
      const displayedIndex = playlist.getDisplayedIndex(listItem.index())
      const song = playlist.songs()[displayedIndex]
      listItem.custom_tooltip(playlist.toString(song))
    }
  })
  // Move to song on click.
  playlistElement.on("click", playlistItem, function() {
    const listItem = $(this)
    const clickedIndex = playlist.getDisplayedIndex(listItem.index())
    if (gplaylist.currentIndex() === clickedIndex)
      return // Clicked song is currently playing.
    playlist.play(clickedIndex)
  })

  function additionalData(media) {
    function aux() {
      const isClassicalPiece = media.composer
      if (!isClassicalPiece)
        return [
          `${media.albumName}${media.discNumber ? "[" + media.discNumber + "]" : ""}`,
          media.track,
          media.year,
        ]

      const titleContainsComposer = media.albumName.toLowerCase().includes(media.composer.toLowerCase())
      const pieceTitle = titleContainsComposer ? media.albumName : `${media.composer}'s ${media.albumName}`
      const opusSuffix = media.opus ? `, ${media.opus}` : ''
      return [
        pieceTitle + opusSuffix,
        media.year,
        media.conductor,
        media.orchestra,
        media.performanceYear,
        media.track,
      ].filter(x => x)
    }
    return aux().concat([media.bitrate + "kbps"]).join(", ")
  }

  playlistUtils.mediaMetadata = media => [
    media.title,
    media.artistName,
  ].concat(additionalData(media)).concat([
    media.duration.timeFormat(),
  ]).join(", ")

  playlistUtils.mediaMetadataHtml = function(media) {
    const isClassicalPiece = media.composer
    const byPrefix = (isClassicalPiece ? "performed by" : "by")
    const metadata = `${byPrefix} <span class="jp-artist">${media.artistName}</span> (<span class="jp-parens">${additionalData(media)}</span>`
    return `<span class='${this.options.playlistOptions.itemClass}' tabindex='1'>`
        + `<span class="width-limited-playlist-span">`
        + `<span class="jp-title">${media.title}</span> <span class="jp-metadata">${metadata}</span>`
        + `</span>`
        + `<span class="jp-duration">, ${media.duration.timeFormat()})</span>`
        + `</span>`
  }
})

playlistUtils = {}
