$(function() {
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
    return jPlayer ? jPlayer.status.currentPercentRelative : undefined
  }
  gplayer.currentPlayingInSeconds = () => player().data().jPlayer.status.currentTime
  const volumeBar = () => $(".jp-volume-bar-value")
  gplayer.getVolume = () => volumeBar().width()
  gplayer.setVolume = v => {
    volumeBar().width(`${v}%`)
    player().jPlayer("volume", v / 100.0)
  }
  gplayer.skip = seconds => {
    player().jPlayer("play", seconds)
  }

  // Playlist
  const pl = () => playlist
  $(function() {
    const p = pl()
    p.getDisplayedIndex = gplaylist.getDisplayedIndex
  })
  gplaylist.currentIndex = () => pl().current
  gplaylist.songs = () => pl().playlist;
  gplaylist.add = (song, playNow) => pl().add(song, playNow);
  gplaylist._next = () => pl().next();
  gplaylist.prev = () => pl().previous();
  gplaylist.clear = () => pl().setPlaylist([], true)
  gplaylist.play = (index) => pl().play(index)
  gplaylist.select = (index) => pl().select(index)

})
