$(function () {
  $("#update_playlist").click(function () {
    $.post("playlist/queue", JSON.stringify(gplaylist.songs().slice(gplaylist.currentIndex()).map(x => x.file)))
  })
  $("#load_playlist").click(function () {
    $.get("playlist/queue", x => x.forEach(e => gplaylist.add(e, false)))
  })
  $("#update_state").click(function () {
    const data = {
      songs: gplaylist.songs().map(x => x.file),
      index: gplaylist.currentIndex(),
      duration: gplayer.currentPlayingRelative()
    }
    $.post("playlist/state", JSON.stringify(data))
  })
})
