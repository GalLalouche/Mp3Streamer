$(function() {
  const last_albums = elem("ul").appendTo($("#last_albums")).css("list-style", "none")
  const add = "plus"
  const remove = "times"
  const download = "download"

  function onMessage(msg) {
    const album = JSON.parse(msg.data)
    const listElement = elem("li", `${icon(add)} ${icon(download)} ${icon(remove)} ${album.artistName}: ${album.title}`).addClass("last-album")
    listElement.find(".fa-" + add).click(function() {
      listElement.remove()
      $.get("data/albums/" + album.dir, e => gplaylist.add(e, false))
    })
    listElement.find(".fa-" + remove).click(function() {
      listElement.remove()
    })
    listElement.find(".fa-" + download).click(function(e) {
      e.preventDefault() // Needed for Chrome so it doesn't follow the link.
      window.location.href = "download/" + album.dir
      listElement.remove()
    })
    last_albums.append(listElement)
    if (listElement.custom_overflown())
      listElement.custom_tooltip(`${album.artistName}: ${album.year} ${album.title}`)
  }

  let last_album_websocket = undefined

  function openLastAlbumConnection() {
    last_album_websocket = openConnection("last_album", onMessage)
  }
  openLastAlbumConnection()

  LastAlbum.reopenLastAlbumWebsocketIfNeeded = function() {
    if (last_album_websocket.readyState === last_album_websocket.CLOSED)
      openLastAlbumConnection()
  }
})

LastAlbum = {}
