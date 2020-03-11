$(function() {
  const div = $("#albums")
  const artists = $("<ol>").appendTo(div)
  const button = (text, clazz) => elem("button", text).addClass(clazz)
  const createHideButton = () => button("Hide", "hide")

  function putArtist(actionType, text, success) {
    $.ajax({
      url: `artist/${actionType}/${text}`,
      data: text,
      type: "PUT",
      contentType: "text/plain",
      dataType: "text",
      success: success
    })
  }

  function addArtist(artistName, albums) {
    const albumsElem = elem("ol")
    elem("li", artistName + " ")
        .append(button("Ignore", "ignore-artist"))
        .append(button("Remove", "remove-artist"))
        .append(createHideButton())
        .append(albumsElem)
        .data("artistName", artistName)
        .appendTo(artists)

    for (const album of albums) {
      elem("li", `[${album.albumType}] ${album.title} (${album.year}) `)
          .data({"artistName": artistName, "year": album.year, "title": album.title})
          .appendTo(albumsElem)
          .append(button("Ignore", "ignore-album"))
          .append(button("Remove", "remove-album"))
          .append(createHideButton())
          .append(button("Google torrent", "google-torrent"))
    }
  }

  $.get("albums/", function(e) {
    Object.keys(e).sort().forEach(artistName => addArtist(artistName, e[artistName]))
  })

  // buttons
  const hideParent = parent => () => parent.hide()

  function onClick(classSelector, f) {
    div.on("click", "." + classSelector, e => f($(e.target).closest("li")))
  }

  onClick("hide", parent => parent.hide())
  onClick("ignore-artist", parent => putArtist("ignore", parent.data("artistName"), hideParent(parent)))
  onClick("remove-artist", parent => putArtist("remove", parent.data("artistName"), hideParent(parent)))
  onClick("ignore-album", parent => putJson("album/ignore", parent.data(), hideParent(parent)))
  onClick("remove-album", parent => putJson("album/remove", parent.data(), hideParent(parent)))

  onClick("google-torrent", parent => {
    const data = parent.data()
    window.open(`https://www.google.com/search?q=${data.artistName} ${data.title} "torrent"`)
  })
})
