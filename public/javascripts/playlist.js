$(function () {
  $("#update_playlist").click(function () {
    const playlist = gplaylist.songs().slice(gplaylist.currentIndex()).map(x => x.file);
    postJson("playlist/queue", playlist)
  })
  $("#load_playlist").click(function () {
    $.get("playlist/queue", x => x.forEach(e => gplaylist.add(e, false)))
  })
  $("#update_state").click(function () {
    const data = {
      songs: gplaylist.songs().map(x => x.file),
      index: gplaylist.currentIndex(),
      duration: gplayer.currentPlayingInSeconds()
    }
    postJson("playlist/state", data)
  })
  $("#load_state").click(function () {
    $.get("playlist/state", data => {
      gplayer.stop()
      gplaylist.setPlaylist(data.songs, false)
      gplaylist.select(data.index)
      gplayer.skip(data.duration)
      // gplayer.playCurrentSong()
    })
  })
})
