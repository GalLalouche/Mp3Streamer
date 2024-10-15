import * as NewAlbumInfo from '/ts/new_albums_info.js'
import {Lyrics} from '/ts/lyrics.js'
import {getDebugAlbum, getDebugSong, isMuted, WAIT_DELAY} from '/ts/initialization.js'
import {Globals} from "/ts/globals.js"

$(function() {
  const fileDownloader = new FileDownloader()
  const randomSongUrl = "data/randomSong"
  const playlist = new JPlayerPlaylist({
    jPlayer: "#jquery_jplayer_1",
    cssSelectorAncestor: "#jp_container_1"
  }, [], {
    swfPath: "../js",
    supplied: "webmv, ogv, m4a, oga, mp3, flac"
  })
  Globals.playlist = playlist
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

  const getMedia = () => $(playlist.cssSelector.jPlayer).data("jPlayer").htmlElement.media

  // On play event hook
  // TODO don't call if the same song?
  $(playlist.cssSelector.jPlayer).data("jPlayer").onPlay = function() {
    const currentPlayingSong = playlist.currentPlayingSong()
    const media = getMedia()
    const songInfo = `${currentPlayingSong.artistName} - ${currentPlayingSong.title}`
    // TODO extract/document
    if (!currentPlayingSong.offline_url) {
      const offline_promise = fileDownloader.download(media.src)
      offline_promise.then(blob => {
        console.log(`Blob for ${songInfo} downloaded`)
        const offline_url = URL.createObjectURL(blob)
        currentPlayingSong.offline_url = offline_url
        if (currentPlayingSong.file === playlist.currentPlayingSong().file && !media.offline_url)
          media.offline_url = offline_url
      })
    } else if (!media.offline_url)
      media.offline_url = currentPlayingSong.offline_url
    $(".jp-currently-playing").html(songInfo)
    document.title = songInfo
    $('#favicon').remove()

    $('head').append(`<link href="${$("img.poster")[0].src}" id="favicon" rel="shortcut icon">`)

    // TODO use plain old observers here
    Lyrics.show(currentPlayingSong)
    External.show(currentPlayingSong)
    Volume.setPeak(currentPlayingSong)
    Score.show(currentPlayingSong)
    NewAlbumInfo.show(currentPlayingSong)
  }
  $(isMuted() ? ".jp-mute" : ".jp-volume-max").click()

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
    const media = getMedia()
    const isSongNearlyFinished = media.duration - media.currentTime < WAIT_DELAY
    if (shouldLoadNextSongFromRandom() && isSongNearlyFinished)
      loadNextRandom(false)
  }, (WAIT_DELAY - 5) * 1000)
})
