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
    elem("li", obj.artistName + " ")
        .append(button("Ignore", "ignore-artist"))
        .append(button("Remove", "remove-artist"))
        .append(createHideButton())
        .append(albums)
        .data("artistName", obj.artistName)
        .appendTo(artists)

    function processAlbum(album) {
      elem("li", `[${album.type}] ${album.title} (${album.year}) `)
          .data({"artistName": obj.artistName, "year": album.year, "title": album.title})
          .appendTo(albums)
          .append(button("Ignore", "ignore-album"))
          .append(button("Remove", "remove-album"))
          .append(createHideButton())
          .append(button("Google torrent", "google-torrent"))
    }

    obj.albums.forEach(processAlbum)
  }

  $.get("albums/", function(e) {
    e.forEach(addArtist)
  })

  // buttons
  const hideParent = parent => () => parent.hide()

  function onClick(classSelector, f) {
    div.on("click", "." + classSelector, () => f($(this).parent()))
  }

  onClick("hide", parent => parent.hide())
  onClick("ignore-artist", parent => putText("artist/ignore", parent.data("artistName"), hideParent(parent)))
  onClick("remove-artist", parent => putText("artist/remove", parent.data("artistName"), hideParent(parent)))
  onClick("ignore-album", parent => putJson("album/ignore", parent.data(), hideParent(parent)))
  onClick("remove-album", parent => putJson("album/remove", parent.data(), hideParent(parent)))

  onClick("google-torrent", parent => {
    const data = parent.data()
    window.open(`https://www.google.com/search?q=${data.artistName} ${data.title} torrent`)
  })
})
