export const WAIT_DELAY = 25
export const isMobile = navigator.userAgent.match(/(iPhone|iPod|iPad|Android|BlackBerry)/) !== null

export function isMuted(): boolean {return window.location.pathname === "/mute"}

const EncodedPlus = encodeURIComponent("+")

// Manually decode + to %2B, since otherwise it will be interpreted as a space
function getSearchParam(key: string): string | null {
  return new URL(window.location.toString().replace("+", EncodedPlus)).searchParams.get(key)
}

/** @return The path of a debug song if exists, null otherwise. */
export function getDebugSong(): string | null {return getSearchParam("addSong")}

/** @return The path of a debug album if exists, null otherwise. */
export function getDebugAlbum(): string | null {return getSearchParam("addAlbum")}

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
