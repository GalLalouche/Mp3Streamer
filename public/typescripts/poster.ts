import * as ColorUtils from "./color-utils.js"
import {GuiEvents, PlayerSetup} from "./gui_events.js"
import {Song} from "./media.js"
import {Player} from "./player.js"

export const PLAYLIST_NAME_KEY = "playlist_name"

export let rgbListeners: ((rgb: RGB) => void)[] = []
// TODO This *really* shouldn't be here, it's just that this button is near the poster :\
export let playlistName: JQuery<HTMLElement>

// FIXME this should be a URL, not string
export function setImage(url: string): void {
  $("#jp_poster_0").attr("src", url)
}

const FAVICON = "favicon"
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
  playlistName = $('#playlist_name')
  playlistName.val(localStorage.getItem(PLAYLIST_NAME_KEY)!)

  poster[0].addEventListener('load', async function () {
    const rgb = await ColorUtils.getColor(poster.attr("src")!)
    const color = rgb.makeLighter(0.5)
    document.body.style.backgroundColor = color.toString()
    rgbListeners.forEach(l => l(color))
  })
  GuiEvents.listen(PlayerSetup, ps => {
    const player = ps.player
    player.listen(pe => {
      if (pe instanceof Song) {
        setImage(pe.poster)
        $('#' + FAVICON).remove()
        $('head')
          .append(`<link href="${($("img.poster")[0] as any).src}" id="${FAVICON}" rel="shortcut icon">`)
      }
    })
  })
})
