// Since jplayer.playlist.js is too freaking big, this extracts my customization
$(function() {
  const playlistElement = $(".jp-playlist")
  const playlist = gplaylist
  playlistElement.on("mouseover", ".playlist-item", function() {
    const el = $(this)
    if (el.custom_overflown()) {
      const index = el.closest("li").index()
      const displayedIndex = playlist.getDisplayedIndex(index)
      const song = playlist.songs()[displayedIndex]
      el.custom_tooltip(playlist.toString(song))
    }
  })
})