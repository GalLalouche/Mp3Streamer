import {Album, Song} from "./types.js"

function confirm(title: string, action: () => void): void {
  let dialog = $(`<div title="Really ${title}?">Are you sure?</div>`)
  dialog.dialog({
    resizable: false,
    height: "auto",
    width: 400,
    modal: true,
    buttons: {
      OK: function () {
        action()
        $(this).dialog("close")
      },
      Cancel: function () {
        $(this).dialog("close")
      },
    },
  })
}

function ignoreAlbum(artist: string, album: string, elementToRemove: JQuery<HTMLElement>): void {
  confirm(
    `ignore ${artist} - ${album}`,
    () => putJson(
      '/new_albums/album/ignore',
      {artistName: artist, title: album},
      () => elementToRemove.remove(),
    ),
  )
}

function ignoreArtist(song: Song): void {
  confirm(
    "ignore " + song.artistName,
    () => $.put(
      '/new_albums/artist/ignore/' + song.artistName,
      () => show(song),
    ),
  )
}

export function show(song: Song): void {
  const fieldSet = $("#new-albums")

  function showAlbums(albums: Album[]): void {
    fieldSet.empty()
    if (albums.length === 0) {
      fieldSet.hide()
      return
    }

    fieldSet.show()
    fieldSet.append(elem("legend", `${albums.length} missing albums for artist`))

    const ul = $("<ul>")
    const ignoreArtistButton = button("Ignore artist")
    ignoreArtistButton.click(() => ignoreArtist(song))
    ul.append(ignoreArtistButton)
    albums.slice(0, 5).forEach(function (album) {
      const albumDate = new Date(album.date)
      const diffTime =
        Math.abs(Date.now() - albumDate.getTime()) / (1000 * 60 * 60 * 24)
      const dateString =
        (Math.ceil(diffTime) >= 365 ? albumDate.getFullYear() : album.date).toString()
      const li = $(
        `<li><span>
                       ${dateString} ${album.title} (${album.albumType}) <button>Ignore</button>
                     </span></li>`,
      )
      li.on('click', 'button', () => ignoreAlbum(song.artistName, album.title, li))
      ul.append(li)
    })
    fieldSet.append(ul)
  }

  fieldSet.empty()
  fieldSet.append(elem("legend", `Fetching new albums for artist...`))
  const requestUrl = "new_albums/albums/" + song.artistName
  $.get(requestUrl, function (albums: string | Album[]) {
    fieldSet.empty()
    if (albums !== "IGNORED") {
      showAlbums(albums as Album[])
      return
    }
    fieldSet.show()
    const b = button("Unignore Artist")
    b.on("click", () => confirm(
      `unignore '${song.artistName}'?`,
      () => $.put(
        '/new_albums/artist/unignore/' + song.artistName,
        result => showAlbums(result),
      )),
    )
    fieldSet.append(b)
  }).fail(function (e: any) {
    fieldSet.empty()
    // For uncaught errors, the responseText is a big ass HTML with a stacktrace.
    const errorMessage = e.responseText.length > 100
      ? `<a href='${requestUrl}' target='_blank'>Click here for HTML</a>`
      : `<br/>${e.responseText}`
    fieldSet.append(elem("legend", `Fetching new albums FAILED... ${errorMessage}`))
    const ignoreArtistButton = button("Ignore artist")
    ignoreArtistButton.click(() => ignoreArtist(song))
    fieldSet.append(ignoreArtistButton)
  })
}
