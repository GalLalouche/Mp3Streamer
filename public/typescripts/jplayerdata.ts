// FIXME this entire file should be expunged.
import * as DataApi from "./data_api.js"
import {getDebugAlbum, getDebugSong, isMuted} from './initialization.js'
import * as Local from "./local.js"
import {Song} from "./media.js"
import {gplayer, gplaylist} from "./player_singleton.js"
import {PlayerEvent} from "./player.js"

// TODO this entire file should split into jplayer specific hacks and more general code

$(function () {
  const JPLAYER_ID = "#jquery_jplayer_1"
  // On play event hook
  gplayer.listen(function (event: PlayerEvent) {
    if (event instanceof Song) {
      const currentPlayingSong = event
      const songInfo = `${currentPlayingSong.artistName} - ${currentPlayingSong.title}`
      Local.setOfflineUrl(currentPlayingSong).then(function () {
        assertDefined(currentPlayingSong.offlineUrl)
        // jplayer hack: update the offlineUrl for the media object
        if ($(JPLAYER_ID).data('jPlayer')) {
          const media = $(JPLAYER_ID).data('jPlayer').htmlElement.media
          if (currentPlayingSong.file === gplaylist.currentPlayingSong().file && media && media.offlineUrl === undefined)
            media.offlineUrl = currentPlayingSong.offlineUrl
        }
      })
      $(".jp-currently-playing").html(songInfo)
      document.title = songInfo
    }
  })

  const debugStartSong = getDebugSong()
  const debugStartAlbum = getDebugAlbum()

  $(isMuted() ? ".jp-mute" : ".jp-volume-max").click()
  assert(gplayer !== undefined, "gplayer is not initialized")
  setup()

  function setup(): void {
    if (debugStartSong) {
      console.log(`Adding debug song <${debugStartSong}>`)
      DataApi.getSong(debugStartSong).then(data => gplaylist.add(data, true))
    } else if (debugStartAlbum) {
      console.log(`Adding debug album <${debugStartAlbum}>`)
      // No idea why this is reversed in the playlist :|
      DataApi.getAlbum("/data/albums/" + debugStartAlbum).then(data => gplaylist.add(data.reverse(), true))
    } else
      // FIXME this isn't *exactly* next, since the playlist has no song playiong, but it's the
      // same code running anyway.
      DataApi.getRandomSong().then(song => gplaylist.add(song, true))
  }
})
