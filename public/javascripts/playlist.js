// Loading and saving playlists, either locally (backup) or from the server.
$(function() {
  $("#update_playlist").click(function() {
    const playlist = gplaylist.songs().slice(gplaylist.currentIndex()).map(x => x.file);
    postJson("playlist/queue", playlist, () => $.toast("Playlist successfully updated"))
  })
  $("#load_playlist").click(function() {
    $.get("playlist/queue", x => x.forEach(e => gplaylist.add(e, false)))
  })

  function getState() {
    return {
      songs: gplaylist.songs(),
      currentIndex: gplaylist.currentIndex(),
      duration: gplayer.currentPlayingInSeconds()
    }
  }
  $("#update_state").click(function() {
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
  $("#load_state").click(function() {
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
  $("#update_backup").click(function() {
    saveBackup()
    $.toast("Backup successfully created")
  })
  $("#load_backup").click(() => {
    const state = loadBackup()
    setState(state)
    Volume.setManualVolume(state.volume)
  })

  const ONE_MINUTE = 60 * 1000
  setInterval(saveBackup, ONE_MINUTE)
})
