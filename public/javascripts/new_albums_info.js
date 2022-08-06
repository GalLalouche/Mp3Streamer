$(function() {
  const fieldSet = $("#new-albums")
  NewAlbumInfo.show = function(song) {
    fieldSet.empty()
    $.get("new_albums/albums/" + song.artistName, function(albums) {
      if (albums.length === 0) {
        fieldSet.hide()
        return
      }

      fieldSet.show()
      fieldSet.append(elem(
          "legend",
          `${albums.length} missing albums for artist`))

      const ul = $("<ul>")
      albums.slice(0, 5).forEach(function(album) {
        const now = new Date()
        const albumDate = new Date(album.date)
        const diffDays = Math.ceil(Math.abs(now - albumDate) / (1000 * 60 * 60 * 24))
        const dateString = diffDays >= 365 ? albumDate.getFullYear() : album.date
        ul.append($(`<li><span>${dateString} ${album.title} (${album.albumType})</span></li>`))
      })
      fieldSet.append(ul)
    })
  }
})
NewAlbumInfo = {}