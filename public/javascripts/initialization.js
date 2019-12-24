playlist = {};
jPlayer = {};
WAIT_DELAY = 25;
isMobile = navigator.userAgent.match(/(iPhone|iPod|iPad|Android|BlackBerry)/) !== null;
const isMuted = () => window.location.pathname === "/mute"

const _getSearchParam = key => new URL(window.location).searchParams.get(key)
/** @return {string} The path of a debug song if exists, null otherwise. */
const getDebugSong = () => _getSearchParam("addSong")
/** @return {string} The path of a debug album if exists, null otherwise. */
const getDebugAlbum = () => _getSearchParam("addAlbum")
// Disables back button
if (window.history && history.pushState) {
  addEventListener('load', function() {
    history.pushState(null, null, null); // creates new history entry with same URL
    addEventListener('popstate', function() {
      alert("Back key disabled!");
      history.pushState(null, null, null);
    });
  });
}
