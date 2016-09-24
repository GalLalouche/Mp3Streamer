class Player {
  load(song) {
    throw new Error("Abstract")
  }
  playCurrentSong() {
    throw new Error("Abstract")
  }
  stop() {
    throw new Error("Abstract")
  }
  pause() {
    throw new Error("Abstract")
  }
  // one of PLAYING, PAUSED, STOPPED
  isPaused() {
    throw new Error("Abstract")
  }
  restart() {
    this.stop();
    this.playCurrentSong();
  }
  togglePause() {
    if (this.isPaused())
      this.playCurrentSong()
    else
      this.pause()
  }
  currentPlayingRelative() {
    throw new Error("Abstract")
  }
  currentPlayingInSeconds() {
    throw new Error("Abstract")
  }
  setVolume(v) {
    throw new Error("Abstract")
  }
  getVolume() {
    throw new Error("Abstract")
  }
  skip(seconds) {
    throw new Error("Abstract")
  }
}
class Playlist {
  clear(instant) {
    this.setPlaylist([], instant)
  }
  setPlaylist(playlist, instant) {
    this.clear()
    const self = this
    playlist.forEach(s => console.log(s))
    playlist.forEach(s => self.add(s, false))
  }
  add(song, playNow) {
    throw new Error("Abstract")
  }
  _next() {
    throw new Error("Abstract")
  }
  next(count) {
    count = count || 1
    for (var i = 0; i < count; i++)
      this._next()
  }
  play(index) {
    throw new Error("Abstract")
  }
  select(index) {
    throw new Error("Abstract")
  }
  prev() {
    throw new Error("Abstract")
  }
  currentIndex() {
    throw new Error("Abstract")
  }
  currentPlayingSong() {
    return this.songs()[this.currentIndex()]
  }
  // returns an array of songs
  songs() {
    throw new Error("Abstract")
  }
}
gplayer = new Player()
gplaylist = new Playlist()
