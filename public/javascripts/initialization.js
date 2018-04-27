playlist = {};
jPlayer = {};
WAIT_DELAY = 25;
isMobile = navigator.userAgent.match(/(iPhone|iPod|iPad|Android|BlackBerry)/) !== null;
mute = window.location.href.endsWith("mute")
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
