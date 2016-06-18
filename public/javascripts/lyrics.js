$(function () {
  const lyrics = $('#lyrics');

  function showLyrics(content) {
    lyrics.html(content);
  }

  Lyrics.show = function (song) {
    showLyrics("Loading lyrics...");
    $.get("lyrics/" + song.file, l => showLyrics(l));
  }

  var interval = null;
  var autoScroll = false;
  function scrollLyrics() {
    clearInterval(interval); // just in case
    interval = setInterval(function scrollLyrics() {
      const heightBuffer = lyrics.height() / 2.0; // don't start scrolling right at the beginning of the song
      const timePercentage = gplayer.currentPlayingRelative() / 100;
      autoScroll = true;
      lyrics.scrollTop((lyrics.prop('scrollHeight') * timePercentage) - heightBuffer);
    }, 1000)
  }
  scrollLyrics();
  // when the *user* scrolls manually, stop auto scrolling for n seconds
  var timeout = null;
  lyrics.scroll(function resetScrollingInterval() {
    if (autoScroll) { // don't trigger on auto scrolls :| bah this is ugly. NO - THIS. IS. JAVASCRIPT!
      autoScroll = false;
      return;
    }
    clearInterval(interval); // stop auto scrolling
    clearTimeout(timeout); // kill any previous request to start auto scrolling
    timeout = setTimeout(scrollLyrics, 5000); // start auto scrolling after 10 seconds
  });
});
Lyrics = {};
