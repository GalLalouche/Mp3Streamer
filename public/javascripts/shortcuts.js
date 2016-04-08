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
    }
  });
  // pauses on poster click
  $(document).on("click", ".poster", function (e) {
    gplayer.togglePause()
  });
});
