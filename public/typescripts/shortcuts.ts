import * as DataApi from "./data_api.js"
import {Song} from "./media.js"
import {gplayer, gplaylist} from "./globals.js"
import * as Search from "./search.js"
import {Events, PlayerControls, PlaylistEvents} from "./events.js"

$(function () {
  $(document).on('keypress', async function (e) {
    {
      const tag = (e.target as any).tagName.toLowerCase()
      const isFromInput = tag === 'input' || tag === 'textarea'

      if (isFromInput) // ignore text input into boxes
        if (e.keyCode === 27) // Esc key
          (e.target as any).blur()
        else
          return
    }
    // Global shortcuts to control the player.
    if (e.ctrlKey) // Don't trigger on e.g., ctrl-c
      return
    const letter = String.fromCharCode(e.which)
    assert(letter.length == 1)
    switch (letter) {
      case 'z':
        Events.publish(PlaylistEvents, "previous")
        break
      case ' ':
      case 'k': // fucking youtube :\
      case 'c':
        Events.publish(PlayerControls, "toggle_pause")
        break
      case 'v':
        Events.publish(PlayerControls, "stop")
        break
      case 'b':
        Events.publish(PlaylistEvents, "next")
        break
      case 'n':
        await loadNextSong()
        break
      case 's':
        Search.quickSearch()
        e.preventDefault()
        break
      case 'S':
        Search.scanPlus()
        e.preventDefault()
        break
    }
  })

  async function loadNextSong(): Promise<void> {
    // verify that the sequence of queued songs, starting from the current song, are continuous in the same album
    // otherwise, don't queue a new song
    for (let index = gplaylist.currentIndex(); index < gplaylist.length() - 1; index++) {
      const currentSong = gplaylist.getSong(index)
      const nextSong = gplaylist.getSong(index + 1)

      function same(field: keyof Song): boolean {return currentSong[field] === nextSong[field]}

      if ((same("artistName") && same("albumName") && currentSong.track + 1 === nextSong.track).isFalse())
        return
    }


    return DataApi.nextSong(gplaylist.last()).then(song => gplaylist.add(song).void())
  }

  $(document).on("click", ".poster", () => gplayer.togglePause())
})
