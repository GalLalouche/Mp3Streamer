// Player
const player = () => $("#jquery_jplayer_1")
gplayer.load = song => player().jPlayer("setMedia", song);
const click = what => $(".jp-" + what).click();
gplayer.pause = () => click("pause");
gplayer.stop = () => click("stop");
gplayer.playCurrentSong = () => click("play");
gplayer.isPaused = () => player().data().jPlayer.status.paused
gplayer.currentPlayingRelative = () => {
  const jPlayer = player().data().jPlayer;
  if (jPlayer)
    return jPlayer.status.currentPercentRelative
  else
    return undefined
}
const volumeBar = () => $(".jp-volume-bar-value")
gplayer.getVolume = () => volumeBar().width()
gplayer.setVolume = v => {
  volumeBar().width(`${v}%`)
  player().jPlayer("volume", v / 100.0)
}

// Playlist
const pl = () => playlist
gplaylist.currentIndex = function () {
  return pl().current
};
gplaylist.songs = () => pl().playlist;
gplaylist.add = (song, playNow) => pl().add(song, playNow);
gplaylist.next = () => pl().next();
gplaylist.prev = () => pl().previous();
