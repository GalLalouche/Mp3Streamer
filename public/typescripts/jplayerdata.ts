// FIXME this entire file should be expunged.
import {Song} from "./media.js"
import {gplayer} from "./player_singleton.js"
import {PlayerEvent} from "./player.js"

// TODO this entire file should split into jplayer specific hacks and more general code

$(function () {
  gplayer.listen(function (event: PlayerEvent) {
    if (event instanceof Song) {
      const currentPlayingSong = event
      const songInfo = `${currentPlayingSong.artistName} - ${currentPlayingSong.title}`
      $(".jp-currently-playing").html(songInfo)
      document.title = songInfo
    }
  })

})
