// Since jplayer.playlist.js is too freaking big, this extracts my customization
$(function() {
  const playlistElement = $(".jp-playlist")
  const playlist = gplaylist
  const playlistItem = ".playlist-item"
  playlistElement.on("mouseover", playlistItem, function() {
    const el = $(this)
    if (el.custom_overflown()) {
      const index = el.closest("li").index()
      const displayedIndex = playlist.getDisplayedIndex(index)
      const song = playlist.songs()[displayedIndex]
      el.custom_tooltip(playlist.toString(song))
    }
  })
  // I must have deleted this somehow :\ Oh well.
  playlistElement.on("click", playlistItem, function() {
    const el = $(this)
    const clickedIndex = playlist.getDisplayedIndex(el.closest("li").index())
    if (gplaylist.currentIndex() === clickedIndex)
      return // Clicked song is currently playing
    playlist.play(clickedIndex)
  })

  playlistUtils.mediaMetadata = function(media) {
    const isClassicalPiece = media.composer
    function additionalData() {
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
    const baseData = [
      media.bitrate + "kbps",
      media.duration.timeFormat(),
    ]
    const allData = additionalData().concat(baseData)

    const byPrefix = (isClassicalPiece ? "performed by" : "by")
    return `${byPrefix} <b>${media.artistName}</b> (${allData.join(", ")})`
  }
})

playlistUtils = {}
