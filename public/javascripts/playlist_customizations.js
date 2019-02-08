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

  playlistUtils.mediaMetdata = function(media) {
    const albumMetadata = [
      `${media.albumName}${media.discNumber ? "[" + media.discNumber + "]" : ""}`,
      media.track,
      media.year,
      media.bitrate + "kbps",
      media.duration.timeFormat(),
    ]
    return `by <b>${media.artistName}</b> (${albumMetadata.join(", ")})`
  }
})

playlistUtils = {}
