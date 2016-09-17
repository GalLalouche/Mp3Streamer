$(function () {
  const lyrics = $('#lyrics')

  function showLyrics(content) {
    autoScroll = true
    lyrics.html(content)
    if (previousContent === lyrics.html()) // Ensure the same HTML formatting is used for comparison
      return // HTML wasn't changed, so don't reset the baselines
    previousContent = lyrics.html() // Ensure the same HTML formatting is used for comparison
    scrollBaseline = 0
    timeBaseline = 0
  }

  Lyrics.show = function (song) {
    autoScroll = true
    lyrics.html("Fetching lyrics...")
    $.get("lyrics/" + song.file, function(l) {
      showLyrics(l)
      scrollLyrics()
    })
  }

  var previousContent = ""
  var autoScroll = false
  var scrollBaseline = 0
  var timeBaseline = 0
  function scrollLyrics() {
    // Don't start scrolling right at the beginning of the song if there is no baseline set
    const heightBaseline =  scrollBaseline || (lyrics.height() / -1.75)
    const timePercentage = (gplayer.currentPlayingRelative() - timeBaseline) / 100.0
    autoScroll = true
    lyrics.scrollTop(lyrics.prop('scrollHeight') * timePercentage + heightBaseline)
  }
  setInterval(scrollLyrics, 100)

  lyrics.scroll(function() { // When the user scrolls manually, reset the baselines
    if (!autoScroll) {
      scrollBaseline = lyrics.scrollTop()
      timeBaseline = scrollBaseline && gplayer.currentPlayingRelative()
    }
    autoScroll = false
  })
})
Lyrics = {}
