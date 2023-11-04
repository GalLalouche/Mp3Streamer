/**
 * A very simple module, showing the most recent album, so it can be easily added to the playlist.
 */
$(function() {
  const lastAlbumDom = $("#last_album")
  let shouldAddNextNewAlbum = false
  let lastAlbumText = undefined
  const ADD = "plus"

  function write(value) {
    lastAlbumDom.empty().append(value)
  }

  const albumText = album => `${icon(ADD)} ${album.artistName}: ${album.title}`

  function addAlbum(album) {
    $.get("data/albums/" + album.dir, e => gplaylist.add(e, false))
  }

  function updateAlbum(album) {
    const text = albumText(album)
    if (text !== lastAlbumText && shouldAddNextNewAlbum) {
      addAlbum(album)
      shouldAddNextNewAlbum = false
    }
    console.log(`Updating last album: '${text}'`)
    const albumElement = elem("span", text)
    write(albumElement)
    albumElement.find(".fa-" + ADD).click(() => addAlbum(album))
    if (albumElement.custom_overflown())
      albumElement.custom_tooltip(albumText)
    lastAlbumText = text
  }

  let last_album_websocket = undefined

  function openLastAlbumConnection() {
    last_album_websocket = openConnection("last_album", msg => updateAlbum(JSON.parse(msg.data)))
  }

  openLastAlbumConnection()
  write(span("Fetching last album..."))

  LastAlbum.reopenLastAlbumWebsocketIfNeeded = function() {
    if (last_album_websocket.readyState === last_album_websocket.CLOSED)
      openLastAlbumConnection()
  }

  LastAlbum.addNextNewAlbum = function() {
    shouldAddNextNewAlbum = true
  }
})

LastAlbum = {}
