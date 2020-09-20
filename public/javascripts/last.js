$(function() {
  const last_albums = elem("ul")
      .appendTo($("#last_albums"))
      .css("list-style", "none")
  const lastAlbums = () => last_albums.children("li")
  const ADD = "plus"
  const REMOVE = "times"
  const DOWNLOAD = "download"
  const clear_all = $(`<i class='fa fa-${REMOVE}'></i><br>`)
      .appendTo(last_albums)
      .custom_tooltip("Clear all new albums")
      .click(function() {
        lastAlbums().remove()
        updateClearAll()
      })
      .css({
        "float": "right",
        "display": "none",
        "font-size": "20px",
      })

  function updateClearAll() {
    clear_all.css("display", lastAlbums().length === 0 ? "none" : "")
  }
  function onMessage(msg) {
    const album = JSON.parse(msg.data)
    const listElement = elem(
        "li",
        `${icon(ADD)} ${icon(DOWNLOAD)} ${icon(REMOVE)} ${album.artistName}: ${album.title}`
    ).addClass("last-album")
    function remove() {
      listElement.remove()
      updateClearAll()
    }
    listElement.find(".fa-" + ADD).click(function() {
      remove()
      $.get("data/albums/" + album.dir, e => gplaylist.add(e, false))
    })
    listElement.find(".fa-" + REMOVE).click(function() {
      remove()
    })
    listElement.find(".fa-" + DOWNLOAD).click(function(e) {
      e.preventDefault() // Needed for Chrome so it doesn't follow the link.
      window.location.href = "download/" + album.dir
      remove()
    })
    last_albums.append(listElement)
    if (listElement.custom_overflown())
      listElement.custom_tooltip(`${album.artistName}: ${album.year} ${album.title}`)
    updateClearAll()
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
