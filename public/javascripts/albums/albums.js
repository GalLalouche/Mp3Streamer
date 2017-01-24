$(function() {
  const div = $("#albums")
  const artists = $("<ol>").appendTo(div)
  const classPrefix = "albums-"
  const button = (text, clazz) => elem("button", text).addClass(classPrefix + clazz)
  const createHideButton = () => button("Hide", "hide")

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
      elem("li", `${album.title} (${album.year})`)
          .data({"artistName": obj.artistName, "year": album.year, "title": album.title})
          .appendTo(albums)
          .append(button("Ignore", "ignore-album"))
          .append(button("Remove", "remove-album"))
          .append(createHideButton())
    }

    obj.albums.forEach(processAlbum)
  }

  $.get("albums", function(e) {
    e.forEach(addArtist)
  })

  // buttons
  div.on("click", ".albums-hide", function() {
    $(this).parent().hide()
  })
  div.on("click", ".albums-ignore-artist", function() {
    const parent = $(this).parent()
    const artistName = parent.data("artistName")
    $.put("artist/ignore", artistName, function() {
      parent.hide()
    })
  })
  div.on("click", ".albums-remove-artist", function() {
    const parent = $(this).parent()
    const artistName = parent.data("artistName")
    $.put("artist/remove", artistName, function() {
      parent.hide()
    })
  })
  div.on("click", ".albums-remove-album", function() {
    const parent = $(this).parent()
    const data = parent.data()
    $.put("album/remove", JSON.stringify(data), function() {
      parent.hide()
    })
  })
})
