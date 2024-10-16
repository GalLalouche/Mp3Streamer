import {gplayer, gplaylist, Song} from "./types.js"

declare class Search {
  static quickSearch(): void
}

$(function () {
  $(document).keypress(function (e) {
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
        gplayer.stop()
        if (gplaylist.currentIndex() > 0) // if not first song, go to previous song; otherwise restart song
          gplaylist.prev()
        gplayer.load(gplaylist.currentPlayingSong())
        gplayer.playCurrentSong()
        break
      case ' ':
      case 'k': // fucking youtube :\
      case 'c':
        gplayer.togglePause()
        break
      case 'v':
        gplayer.stop()
        break
      case 'b':
        gplaylist.next()
        break
      case 'n':
        loadNextSong()
        break
      case 's':
        Search.quickSearch()
        e.preventDefault()
    }
  })

  function loadNextSong() {
    const songs = gplaylist.songs()
    // verify that the sequence of queued songs, starting from the current song, are continuous in the same album
    // otherwise, don't queue a new song
    for (let index = gplaylist.currentIndex(); index < gplaylist.length() - 1; index++) {
      const currentSong = songs[index]
      const nextSong = songs[index + 1]

      function same(field: keyof Song): boolean {return currentSong[field] === nextSong[field]}

      if ((same("artistName") && same("albumName") && currentSong.track + 1 === nextSong.track).isFalse())
        return
    }
    $.get("data/nextSong?path=" + gplaylist.last().file, function (song) {
      gplaylist.add(song, false)
    })
  }

  $(document).on("click", ".poster", () => gplayer.togglePause())
})
