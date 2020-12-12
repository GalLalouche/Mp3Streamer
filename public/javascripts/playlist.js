// Loading and saving playlists, either locally (backup) or from the server.
$(function() {
  const body = $("body")
  function listenToClick(id, callback) {
    body.on("click", "button#" + id, callback)
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

  listenToClick("update_clipboard", function() {
    const state = getState()
    state.volume = Volume.getVolumeBaseline()
    const json = JSON.stringify(state)
    copyTextToClipboard(json)
    console.log(json) // Backup in case the clipboard somehow gets deleted.
    $.toast("Copied json to clipboard")
  })
  listenToClick("load_clipboard", function() {
    function getJsonPromise() {
      return new Promise(resolve => {
        const dialog = $("<div><input type='text' placeholder='put json here' /></div>")
        dialog.dialog({
          autoOpen: true,
          height: 100,
          width: 200,
          modal: true,
        })
        dialog.on('input', 'input', function() {
          const that = $(this)
          const val = that.val()
          that.parent("div").dialog("close")
          resolve(val)
        })
      })
    }

    getJsonPromise()
        .then(text => {
          const state = JSON.parse(text)
          setState(state)
          Volume.setManualVolume(state.volume)
        })
        .catch(err => console.error('Failed to load JSON', err))
  })

  const ONE_MINUTE = 60 * 1000
  setInterval(saveBackup, ONE_MINUTE)
})
