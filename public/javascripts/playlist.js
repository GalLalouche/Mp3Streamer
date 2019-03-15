// Loading and saving playlists, either locally (backup) or from the server.
$(function() {
  const body = $("body")
  function listenToClick(id, callback) {
    body.on("click", "#" + id, callback)
  }
  listenToClick("update_playlist", function() {
    const playlist = gplaylist.songs().slice(gplaylist.currentIndex()).map(x => x.file);
    postJson("playlist/queue", playlist, () => $.toast("Playlist successfully updated"))
  })
  listenToClick("load_playlist", function() {
    $.get("playlist/queue", x => x.forEach(e => gplaylist.add(e, false)))
  })

  function getState() {
    return {
      songs: gplaylist.songs(),
      currentIndex: gplaylist.currentIndex(),
      duration: gplayer.currentPlayingInSeconds()
    }
  }
  listenToClick("update_state", function() {
    const state = getState()
    state.songs = state.songs.map(x => x.file)
    postJson("playlist/state", state, () => $.toast("State successfully updated"))
  })

  function setState(state) {
    gplayer.stop()
    gplaylist.setPlaylist(state.songs, false)
    gplaylist.select(state.currentIndex)
    gplayer.skip(state.duration)
    // gplayer.playCurrentSong()
  }
  listenToClick("load_state", function() {
    $.get("playlist/state", setState)
  })

  const backupKey = "backup"
  function saveBackup() {
    const state = getState()
    state.volume = Volume.getVolumeBaseline()
    localStorage.setItem(backupKey, JSON.stringify(state))
  }
  function loadBackup() {
    return JSON.parse(localStorage.getItem(backupKey))
  }
  listenToClick("update_backup", function() {
    saveBackup()
    $.toast("Backup successfully created")
  })
  listenToClick("load_backup", function() {
    const state = loadBackup()
    setState(state)
    Volume.setManualVolume(state.volume)
  })

  const ONE_MINUTE = 60 * 1000
  setInterval(saveBackup, ONE_MINUTE)
})
