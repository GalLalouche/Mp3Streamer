$(function() {
  const click = what => $(".jp-" + what).click()
  const isFromInput = tag => tag == 'input' || tag == 'textarea'
  const isPaused = () => $('#jquery_jplayer_1').data().jPlayer.status.paused
  function togglePlay() { isPaused() ? click("play") : click("pause") }

  $(document).keyup(function(e) {
    const jPlayer = $('#jquery_jplayer_1').data().jPlayer;
    if (isFromInput(e.target.tagName.toLowerCase()))
      return
    const letter = String.fromCharCode(e.which);
    switch (letter) {
    case 'Z':
      if (playlist.current > 0) // if not first song, go to previous song; otherwise restart song
        click("previous");
      else {
        click("stop");
        click("play");
      }
      break;
    case 'X': // restart song if playing, otherwise unpause
      if (isPaused()) {
        click("play");
      } else {
        click("stop");
        click("play");
      }
      break;
    case 32: // Space key
    case 'K': // fucking youtube :\
    case 'C':
      togglePlay()
      break;
    case 'V':
      click("stop");
      break;
    case 'B':
      click("next");
      break;
    }
  });

  // technically not a shortcut, but oh well
  $(document).on("click", ".poster", function(e) {
    togglePlay();
  });
});
