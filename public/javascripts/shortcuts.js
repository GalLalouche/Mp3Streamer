$(function() {
  const click = what => $(".jp-" + what).click()
  const isFromInput = tag => tag == 'input' || tag == 'textarea'

  $(document).keyup(function(e) {
    if (isFromInput(e.target.tagName.toLowerCase()))
      return
    const jPlayer = $('#jquery_jplayer_1').data().jPlayer;
    const letter = String.fromCharCode(e.which);
    switch (letter) {
    case 'Z':
      if (playlist.current > 0) // if not first song, go to previous song
        click("previous");
      else { // otherwise, restart song
        click("stop");
        click("play");
      }
      break;
    case 'X': // restart song if playing, otherwise unpause
      if (jPlayer.status.paused) {
        click("play");
      } else {
        click("stop");
        click("play");
      }
      break;
    case 32: // Space key
    case 'K': // fucking youtube :\
    case 'C':
      if (jPlayer.status.paused)
        click("play");
      else
        click("pause");
      break;
    case 'V':
      click("stop");
      break;
    case 'B':
      click("next");
      break;
    }
  });
});
