$(function() {
  const fieldSet = $("#new-albums")

  function confirm(title, action) {
    let dialog = $(`<div title="Really ${title}?">Are you sure?</div>`)
    dialog.dialog({
      resizable: false,
      height: "auto",
      width: 400,
      modal: true,
      buttons: {
        OK: function() {
          action()
          $(this).dialog("close")
        },
        Cancel: function() {
          $(this).dialog("close")
        }
      }
    })
  }

  function ignoreAlbum(artist, album, elementToRemove) {
    confirm(
        `ignore ${artist} - ${album}`,
        () => putJson(
            '/new_albums/album/ignore',
            {artistName: artist, title: album},
            () => elementToRemove.remove()
        )
    )
  }

  function ignoreArtist(song) {
    confirm(
        "ignore " + song.artistName,
        () => $.put(
            '/new_albums/artist/ignore/' + song.artistName,
            () => NewAlbumInfo.show(song)
        ),
    )
  }

  NewAlbumInfo.show = function(song) {
    function showAlbums(albums) {
      fieldSet.empty()
      if (albums.length === 0) {
        fieldSet.hide()
        return
      }

      fieldSet.show()
      fieldSet.append(elem(
          "legend",
          `${albums.length} missing albums for artist`))

      const ul = $("<ul>")
      const ignoreArtistButton = button("Ignore artist")
      ignoreArtistButton.click(() => ignoreArtist(song))
      ul.append(ignoreArtistButton)
      albums.slice(0, 5).forEach(function(album) {
        const now = new Date()
        const albumDate = new Date(album.date)
        const diffDays = Math.ceil(Math.abs(now - albumDate) / (1000 * 60 * 60 * 24))
        const dateString = diffDays >= 365 ? albumDate.getFullYear() : album.date
        const li =
            $(`<li><span>
               ${dateString} ${album.title} (${album.albumType}) <button>Ignore</button>
               </span></li>`)
        li.on('click', 'button', () => ignoreAlbum(song.artistName, album.title, li))
        ul.append(li)
      })
      fieldSet.append(ul)
    }

    fieldSet.empty()
    fieldSet.append(elem("legend", `Fetching new albums for artist...`))
    $.get("new_albums/albums/" + song.artistName, function(albums) {
      fieldSet.empty()
      if (albums !== "IGNORED") {
        showAlbums(albums)
        return
      }
      fieldSet.show()
      const b = button("Unignore Artist")
      b.on("click", () => confirm(
          `unignore '${song.artistName}'?`,
          () => $.put('/new_albums/artist/unignore/' + song.artistName, result => showAlbums(result)))
      )
      fieldSet.append(b)
    })
  }
})
NewAlbumInfo = {}