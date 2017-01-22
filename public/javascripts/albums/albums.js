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
        .appendTo(artists)
        .data(obj.artistName)

    function processAlbum(album) {
      elem("li", `${album.title} (${album.year})`)
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
    const artistName = $(this).data()
    console.log("Removing " + artistName)
  })
})
