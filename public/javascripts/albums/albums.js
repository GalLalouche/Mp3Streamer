$(function() {
  const div = $("#albums")
  const artists = $("<ol>").appendTo(div)
  const button = (text, clazz) => elem("button", text).addClass(clazz)
  const createHideButton = () => button("Hide", "hide")

  function putText(url, text, success) {
    $.ajax({
      url: url,
      data: text,
      type: "PUT",
      contentType: "text/plain",
      dataType: "text",
      success: success
    })
  }
  function addArtist(obj) {
    const albums = elem("ol")
    elem("li", obj.artistName)
        .append(button("Ignore", "ignore-artist"))
        .append(button("Remove", "remove-artist"))
        .append(createHideButton())
        .append(albums)
        .data("artistName", obj.artistName)
        .appendTo(artists)

    function processAlbum(album) {
      elem("li", `[${album.type}] ${album.title} (${album.year})`)
          .data({"artistName": obj.artistName, "year": album.year, "title": album.title})
          .appendTo(albums)
          .append(button("Ignore", "ignore-album"))
          .append(button("Remove", "remove-album"))
          .append(createHideButton())
          .append(button("Google torrent", "google-torrent"))
    }

    obj.albums.forEach(processAlbum)
  }

  $.get("albums", function(e) {
    e.forEach(addArtist)
  })

  // buttons
  div.on("click", ".hide", function() {
    $(this).parent().hide()
  })
  div.on("click", ".ignore-artist", function() {
    const parent = $(this).parent()
    const artistName = parent.data("artistName")
    putText("artist/ignore", artistName, function() {
      parent.hide()
    })
  })
  div.on("click", ".remove-artist", function() {
    const parent = $(this).parent()
    const artistName = parent.data("artistName")
    putText("artist/remove", artistName, function() {
      parent.hide()
    })
  })
  div.on("click", ".ignore-album", function() {
    const parent = $(this).parent()
    const data = parent.data()
    putJson("album/ignore", data, function() {
      parent.hide()
    })
  })
  div.on("click", ".remove-album", function() {
    const parent = $(this).parent()
    const data = parent.data()
    putJson("album/remove", data, function() {
      parent.hide()
    })
  })
  div.on("click", ".google-torrent", function() {
    const parent = $(this).parent()
    const data = parent.data()
    window.open("https://www.google.com/search?q=torrent " + data.title)
  })
})
