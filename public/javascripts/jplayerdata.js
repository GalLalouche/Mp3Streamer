$(function() {
  const randomSongUrl = "data/randomSong"
  // Initialize playlist
  // noinspection JSUndeclaredVariable
  playlist = new JPlayerPlaylist({ // Explicitly global.
    jPlayer: "#jquery_jplayer_1",
    cssSelectorAncestor: "#jp_container_1"
  }, [], {
    swfPath: "../js",
    supplied: "webmv, ogv, m4a, oga, mp3, flac"
  })
  // Modify next to fetch a random song if in shuffle mode and at the last song
  // TODO move to playlist_customization
  playlist.oldNext = playlist.next
  const shouldLoadNextSongFromRandom = () => playlist.isLastSongPlaying()
  playlist.next = function() {
    if (shouldLoadNextSongFromRandom())
      loadNextRandom(true)
    else
      playlist.oldNext()
  }
  // On play event hook
  // TODO don't call if the same song?
  $(playlist.cssSelector.jPlayer).data("jPlayer").onPlay = function() {
    const currentlyPlayingSong = playlist.currentPlayingSong()
    const songInfo = `${currentlyPlayingSong.artistName} - ${currentlyPlayingSong.title}`
    $(".jp-currently-playing").html(songInfo)
    document.title = songInfo
    $('#favicon').remove()

    $('head').append(`<link href="${$("img.poster")[0].src}" id="favicon" rel="shortcut icon">`)

    Lyrics.show(currentlyPlayingSong)
    External.show(currentlyPlayingSong)
    Volume.setPeak(currentlyPlayingSong)
  }
  $("#jquery_jplayer_1").addClass("poster")
  // Mute for debugging if requested
  if (mute)
    $(".jp-mute").click()
  else
    $(".jp-volume-max").click()

  function loadNextRandom(playNow) {
    $.get(randomSongUrl, function(data) {
      playlist.add(data, playNow)
    })
  }

  const debugStartSong = getDebugSong()
  const debugStartAlbum = getDebugAlbum()
  if (debugStartSong) {
    console.log(`Adding debug song <${debugStartSong}>`)
    $.get("/data/songs/" + debugStartSong, data => gplaylist.add(data, true))
  } else if (debugStartAlbum) {
    console.log(`Adding debug album <${debugStartAlbum}>`)
    // No idea why this is reversed in the playlist :|
    $.get("/data/albums/" + debugStartAlbum, data => gplaylist.add(data.reverse(), true))
  } else
    loadNextRandom(true)
  // Fetches new songs before current song ends.
  setInterval(function() {
    const media = $(playlist.cssSelector.jPlayer).data("jPlayer").htmlElement.media
    const isSongNearlyFinished = media.duration - media.currentTime < WAIT_DELAY
    if (shouldLoadNextSongFromRandom() && isSongNearlyFinished)
      loadNextRandom(false)
  }, (WAIT_DELAY - 5) * 1000)
})
