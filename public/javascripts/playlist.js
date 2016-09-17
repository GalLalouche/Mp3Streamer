$(function () {
  $("#update_playlist").click(function() {
    $.post("playlist", JSON.stringify(gplaylist.songs().slice(gplaylist.currentIndex()).map(x => x.file)))
  })
  $("#load_playlist").click(function() {
    $.get("playlist", x => x.forEach(e => gplaylist.add(e, false)))
  })
})
