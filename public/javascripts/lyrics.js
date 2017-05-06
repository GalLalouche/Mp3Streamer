$(function () {
  const lyricsDiv = $('#lyrics')
  const lyrics = $('<div style="overflow-y: scroll; height:770px;"/>').appendTo(lyricsDiv)
  lyricsDiv.append(elem('br'))
  const lyricsPusher = elem('div').appendTo(lyricsDiv)
  const lyricsUrlBox = $("<input id='lyrics-url' placeholder='Lyrics URL' type='text'/>").appendTo(lyricsPusher)
  const updateLyricsButton = elem("button", "Update lyrics").appendTo(lyricsPusher)
  validateBoxAndButton(lyricsUrlBox, updateLyricsButton, isValidUrl, updateLyrics)

  function updateLyrics() {
    const url = $("#lyrics-url").val()
    const songPath = gplaylist.currentPlayingSong().file
    $.ajax({
      type: "POST",
      url: "lyrics/push/" + songPath,
      data: url,
      success: c => showLyrics(c),
      contentType: "text/plain",
    })
  }


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
    $.get("lyrics/" + song.file, function (l) {
      showLyrics(l)
      scrollLyrics()
    })
  }

  let previousContent = ""
  let autoScroll = false
  let scrollBaseline = 0
  let timeBaseline = 0

  function scrollLyrics() {
    // Don't start scrolling right at the beginning of the song if there is no baseline set
    const heightBaseline = scrollBaseline || (lyrics.height() / -1.75)
    const timePercentage = (gplayer.currentPlayingRelative() - timeBaseline) / 100.0
    autoScroll = true
    lyrics.scrollTop(lyrics.prop('scrollHeight') * timePercentage + heightBaseline)
  }

  setInterval(scrollLyrics, 100)

  lyrics.scroll(function () { // When the user scrolls manually, reset the baselines
    if (!autoScroll) {
      scrollBaseline = lyrics.scrollTop()
      timeBaseline = scrollBaseline && gplayer.currentPlayingRelative()
    }
    autoScroll = false
  })
})
Lyrics = {}
