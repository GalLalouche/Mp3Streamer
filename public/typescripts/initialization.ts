import * as DataApi from "./data_api.js"
import {Startup} from "./events.js"
import {addAndPlayNow} from "./globals.js"
import {Song} from "./media.js"

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

Startup.promise.then(async _ => {
  const debugStartSong = getSearchParam("addSong")
  const debugStartAlbum = getSearchParam("addAlbum")

  $(isMuted() ? ".jp-mute" : ".jp-volume-max").trigger('click')

  async function initialSongs(): Promise<Song | Song[]> {
    if (debugStartSong) {
      console.log(`Adding debug song <${debugStartSong}>`)
      return DataApi.getSong(debugStartSong)
    } else if (debugStartAlbum) {
      console.log(`Adding debug album <${debugStartAlbum}>`)
      // No idea why this is reversed in the playlist :|
      return DataApi.getAlbum("/data/albums/" + debugStartAlbum).then(data => data.reverse())
    } else
      return DataApi.getRandomSong()
  }
  initialSongs().then(addAndPlayNow)
})
