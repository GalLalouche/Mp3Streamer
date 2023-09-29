/**
 * A very simple module, showing the most recent album, so it can be easily added to the playlist.
 */
$(function () {
  const last_album = $("#last_album")
  const ADD = "plus"

  function write(value) {
    last_album.empty().append(value)
  }

  function updateAlbum(album) {
    const albumText = `${album.artistName}: ${album.year} ${album.title}`
    console.log(`Updating last album: '${albumText}'`)
    const albumElement = elem(
        "span",
        `${icon(ADD)} ${album.artistName}: ${album.title}`
    )
    write(albumElement)
    albumElement.find(".fa-" + ADD).click(() =>
        $.get("data/albums/" + album.dir, e => gplaylist.add(e, false))
    )
    if (albumElement.custom_overflown())
      albumElement.custom_tooltip(albumText)
  }

  let last_album_websocket = undefined

  function openLastAlbumConnection() {
    last_album_websocket = openConnection("last_album", msg => updateAlbum(JSON.parse(msg.data)))
  }

  openLastAlbumConnection()
  write(span("Fetching last album..."))

  LastAlbum.reopenLastAlbumWebsocketIfNeeded = function () {
    if (last_album_websocket.readyState === last_album_websocket.CLOSED)
      openLastAlbumConnection()
  }
})

LastAlbum = {}
