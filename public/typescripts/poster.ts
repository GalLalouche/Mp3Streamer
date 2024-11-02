export const PLAYLIST_NAME_KEY = "playlist_name.js"

export class Poster {
  static rgbListeners: ((rgb: RGB) => void)[] = []
  // TODO This *really* shouldn't be here, it's just that this button is near the poster :\
  static playlistName: JQuery<HTMLElement>
}

$exposeGlobally!(Poster)

waitForElem("#jp_poster_0").then(p => $(p)).then(poster => {
  function buttonAux(id: string, text: string): JQuery<HTMLElement> {
    return button({"id": id}, text)
  }

  poster.addClass("poster")
  const div = poster.closest("div")
  const posterAndButtonsDiv = table({"id": "poster-table"}).append(
    tr().append(
      td({"class": "poster-buttons left-poster-buttons"}).append(
        $("<input id=playlist_name placeholder='Remote playlist name' class='poster-buttons'>"),
        buttonAux("update_backup", "Update backup"),
      ),
      td().append(poster),
      td({"class": "poster-buttons right-poster-buttons"}).append(
        buttonAux("load_playlist", "Load playlist"),
        buttonAux("load_backup", "Load backup"),
      ),
    ),
  )
  div.prepend(posterAndButtonsDiv)
  Poster.playlistName = $('#playlist_name')
  Poster.playlistName.val(localStorage.getItem(PLAYLIST_NAME_KEY)!)

  poster[0].addEventListener('load', async function () {
    const rgb = await ColorGetter.getColorAsync(poster.attr("src")!)
    const color = rgb.makeLighter(0.5)
    document.body.style.backgroundColor = color.toString()
    Poster.rgbListeners.forEach(l => l(color))
  })
})
