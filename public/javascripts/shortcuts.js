$(function () {
  const isFromInput = tag => tag == 'input' || tag == 'textarea'

  $(document).keyup(function (e) {
    if (isFromInput(e.target.tagName.toLowerCase())) // ignore text input into boxes
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
    }
  });
  function loadNextSong() {
    const songs = gplaylist.songs()
    // verify that the sequence of queued songs, starting from the current song, are continuous in the same album
    // otherwise, don't queue a new song
    for (let index = gplaylist.currentIndex(); index < gplaylist.length() - 1; index++) {
      const currentSong = songs[index]
      const nextSong = songs[index + 1]
      const same = field => currentSong[field] == nextSong[field]
      if (false == (same("artistName") && same("albumName") && currentSong.track + 1 == nextSong.track))
        return
    }
    $.get("data/nextSong?path=" + gplaylist.last().file, function (song) {
      gplaylist.add(song, false)
    })
  }

  // pauses on poster click
  $(document).on("click", ".poster", function (e) {
    gplayer.togglePause()
  });
});
