$(function() {
  $(document).keyup(function(e) {
    {
      const tag = e.target.tagName.toLowerCase()
      const isFromInput = tag === 'input' || tag === 'textarea'

      if (isFromInput) // ignore text input into boxes
        if (e.keyCode === 27) // Esc key
          e.target.blur()
        else
          return
    }
    // Global shortcuts to control the player.
    if (e.ctrlKey) // Don't trigger on e.g., ctrl-c
      return
    const letter = String.fromCharCode(e.which);
    switch (letter) {
      case 'Z':
        gplayer.stop();
        if (gplaylist.currentIndex() > 0) // if not first song, go to previous song; otherwise restart song
          gplaylist.prev()
        gplayer.load(gplaylist.currentPlayingSong())
        gplayer.playCurrentSong()
        break;
      case 32: // Space key
      case ' ':
      case 'K': // fucking youtube :\
      case 'C':
        gplayer.togglePause()
        break;
      case 'V':
        gplayer.stop()
        break;
      case 'B':
        gplaylist.next()
        break;
      case 'N':
        loadNextSong()
        break;
      case 'S':
        Search.quickSearch()
    }
  });

  function loadNextSong() {
    const songs = gplaylist.songs()
    // verify that the sequence of queued songs, starting from the current song, are continuous in the same album
    // otherwise, don't queue a new song
    for (let index = gplaylist.currentIndex(); index < gplaylist.length() - 1; index++) {
      const currentSong = songs[index]
      const nextSong = songs[index + 1]
      const same = field => currentSong[field] === nextSong[field]
      if ((same("artistName") && same("albumName") && currentSong.track + 1 === nextSong.track).isFalse())
        return
    }
    $.get("data/nextSong?path=" + gplaylist.last().file, function(song) {
      gplaylist.add(song, false)
    })
  }

  // pauses on poster click
  $(document).on("click", ".poster", function(e) {
    gplayer.togglePause()
  });
});
