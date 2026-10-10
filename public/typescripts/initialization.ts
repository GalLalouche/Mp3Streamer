import * as DataApi from "./data_api.js"
import {GuiEvents, PlayerSetup} from "./gui_events.js"

export const isMobile = navigator.userAgent.match(/(iPhone|iPod|iPad|Android|BlackBerry)/) !== null

export function isMuted(): boolean {return window.location.pathname.includes("/mute")}

export function isLocalHost(): boolean {
  return window.location.host.toLowerCase().startsWith("localhost")
}

const EncodedPlus = encodeURIComponent("+")

// Manually decode + to %2B, since otherwise it will be interpreted as a space
function getSearchParam(key: string): string | null {
  return new URL(window.location.toString().replace("+", EncodedPlus)).searchParams.get(key)
}

// Disables back button
if (window.history && history.pushState) {
  addEventListener('load', function () {
    function populateHistoryWithSameUrl(): void {history.pushState(null, "", null)}

    populateHistoryWithSameUrl()
    addEventListener('popstate', function () {
      alert("Back key disabled!")
      populateHistoryWithSameUrl()
    })
  })
}

GuiEvents.listen(PlayerSetup, ps => {
  const playlist = ps.playlist
  const debugStartSong = getSearchParam("addSong")
  const debugStartAlbum = getSearchParam("addAlbum")

  $(isMuted() ? ".jp-mute" : ".jp-volume-max").click()

  if (debugStartSong) {
    console.log(`Adding debug song <${debugStartSong}>`)
    DataApi.getSong(debugStartSong).then(data => playlist.add(data, true))
  } else if (debugStartAlbum) {
    console.log(`Adding debug album <${debugStartAlbum}>`)
    // No idea why this is reversed in the playlist :|
    DataApi.getAlbum("/data/albums/" + debugStartAlbum).then(data => playlist.add(data.reverse(), true))
  } else
    // FIXME this isn't *exactly* next, since the playlist has no song playiong, but it's the
    //  same code running anyway.
    DataApi.getRandomSong().then(song => playlist.add(song, true))
})
