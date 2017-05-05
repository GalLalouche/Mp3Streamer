$(function () {
  const lyricsDiv = $('#lyrics')
  const lyrics = elem('div').appendTo(lyricsDiv)
  const lyricsPusher = elem('div').appendTo(lyricsDiv)
  lyricsPusher.append($("<input id='lyrics-url' placeholder='Lyrics URL' type='text'/><br/>"))
  lyricsPusher.append(elem("button", "Update lyrics").click(updateLyrics).attr("id", "update-lyrics").prop("disabled", true))
  // Update recon on pressing Enter
  lyricsPusher.on("keyup", "#lyrics-url", function(event) {
    // TODO move to common
    if (event.keyCode == 13) { // Enter
      updateLyrics()
    } else {
      verify($(this))
    }
  })
  const updateLyricsButton = $("#update-lyrics")
  function verify(element) {
    updateLyricsButton.prop('disabled', isValidUrl(element.val()))
  }
  function updateLyrics() {
    if (updateLyricsButton.prop("disabled"))
      return
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
    $.get("lyrics/" + song.file, function(l) {
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
