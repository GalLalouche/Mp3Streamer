$(function() {
  const lyricsDiv = $('#lyrics')
  const lyricsContent = $('<div style="overflow-y: scroll; height:770px;"/>').appendTo(lyricsDiv)
  appendBr(lyricsDiv)
  const lyricsPusher = div().appendTo(lyricsDiv)
  const lyricsUrlBox = $("<input id='lyrics-url' placeholder='Lyrics URL' type='text'/>").appendTo(lyricsPusher)
  const updateLyricsButton = button("Update lyrics").appendTo(lyricsPusher)
  const instrumentalSongButton = button("Instrumental Song").appendTo(lyricsPusher)
  const instrumentalArtistButton = button("Instrumental Artist").appendTo(lyricsPusher)
  validateBoxAndButton(lyricsUrlBox, updateLyricsButton, isValidUrl, updateLyrics)

  lyricsUrlBox.keyup(function() { // the validateBoxAndButton only applies to updateLyricsButton
    const box = $(this)
    function disable(b) {
      b.prop('disabled', box.val() !== "")
    }
    disable(instrumentalArtistButton)
    disable(instrumentalSongButton)
  })

  function setInstrumental(type) {
    $.post(`lyrics/instrumental/${type}/${gplaylist.currentPlayingSong().file}`, null, c => showLyrics(c))
  }
  instrumentalSongButton.click(() => setInstrumental("song"))
  instrumentalArtistButton.click(() => setInstrumental("artist"))
  function updateLyrics() {
    const url = lyricsUrlBox.val()
    const songPath = gplaylist.currentPlayingSong().file
    $.ajax({
      type: "POST",
      url: "lyrics/push/" + songPath,
      data: url,
      success: c => showLyrics(c),
      contentType: "text/plain",
    })
    clearButtons()
    lyricsContent.html("Pushing lyrics...")
  }

  function clearButtons() {
    lyricsUrlBox.val("")
    lyricsUrlBox.trigger("keyup")
  }


  function showLyrics(content) {
    clearButtons()
    autoScroll = true
    lyricsContent.html(content)
    if (previousContent === lyricsContent.html()) // Ensure the same HTML formatting is used for comparison
      return // HTML wasn't changed, so don't reset the baselines
    previousContent = lyricsContent.html() // Ensure the same HTML formatting is used for comparison
    scrollBaseline = 0
    timeBaseline = 0
  }

  Lyrics.show = function(song) {
    clearButtons()
    autoScroll = true
    lyricsContent.html("Fetching lyrics...")
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
    const heightBaseline = scrollBaseline || (lyricsContent.height() / -1.75)
    const timePercentage = (gplayer.currentPlayingRelative() - timeBaseline) / 100.0
    autoScroll = true
    lyricsContent.scrollTop(lyricsContent.prop('scrollHeight') * timePercentage + heightBaseline)
  }

  setInterval(scrollLyrics, 100)

  lyricsContent.scroll(function() { // When the user scrolls manually, reset the baselines
    if (!autoScroll) {
      scrollBaseline = lyricsContent.scrollTop()
      timeBaseline = scrollBaseline && gplayer.currentPlayingRelative()
    }
    autoScroll = false
  })
})
Lyrics = {}
