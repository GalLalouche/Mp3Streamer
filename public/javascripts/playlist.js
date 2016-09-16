$(function () {
  $("#update_playlist").click(function() {
    $.post("playlist", JSON.stringify(gplaylist.songs().slice(gplaylist.currentIndex()).map(x => x.file)))
  })
})
