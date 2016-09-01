gplayer.load = song => $("#jquery_jplayer_1").jPlayer("setMedia", song);
const click = what => $(".jp-" + what).click();
gplayer.pause = () => click("pause");
gplayer.stop = () => click("stop");
gplayer.playCurrentSong = () => click("play");
gplayer.isPaused = () => $('#jquery_jplayer_1').data().jPlayer.status.paused
gplayer.currentPlayingRelative = () => {
  const jPlayer = $('#jquery_jplayer_1').data().jPlayer;
  if(jPlayer)
    return jPlayer.status.currentPercentRelative
  else
    undefined
}
gplaylist.currentIndex = function() {
  return playlist.current
};
gplaylist.songs =() => playlist.playlist;
gplaylist.add = (song, playNow) => playlist.add(song, playNow);
gplaylist.next = () => playlist.next();
gplaylist.prev = () => playlist.previous();
