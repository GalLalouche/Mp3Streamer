// Loading and saving playlists, either locally (backup) or from the server.
$(function() {
  const body = $("body")

  function listenToClick(id, callback) {
    body.on("click", "button#" + id, callback)
  }

  listenToClick("load_playlist", function() {
    $.get("playlist/", ids => chooseState(ids))
  })

  function chooseState(ids) {
    // Create the dialog div
    const $dialog = div({id: 'dialog', title: 'Select a playlist'})

    for (const id of ids) {
      $dialog.append($('<button>', {
        text: id,
        click: function() {
          loadPlaylist(id)
          $dialog.dialog("close")
        }
      })).append(br)
    }

    $dialog.dialog({autoOpen: true, modal: true})
    $dialog.on('dialogclose', () => $dialog.remove())
    $dialog.dialog("open")
  }

  function loadPlaylist(id) {
    $.get("playlist/" + id, function(playlist) {
      setState(playlist)
    })
  }

  function getState() {
    return {
      songs: gplaylist.songs(),
      currentIndex: gplaylist.currentIndex(),
      duration: gplayer.currentPlayingInSeconds()
    }
  }

  function setState(state) {
    state.songs.forEach(song => song.offline_url = null)
    gplayer.stop()
    gplaylist.setPlaylist(state.songs, false)
    gplaylist.select(state.currentIndex)
    gplayer.skip(state.duration)
    // gplayer.playCurrentSong()
  }

  const backupKey = "backup"

  function saveBackup() {
    const state = getState()
    if (state.songs.length === 0) {
      console.log("Won't save empty backup")
      return
    }
    state.volume = Volume.getVolumeBaseline()
    localStorage.setItem(backupKey, JSON.stringify(state))
    const playlistName = Poster.playlistName.val()
    if (playlistName) {
      localStorage.setItem(PLAYLIST_NAME_KEY, playlistName)
      console.log(`Saving playlist ${playlistName} remotely`)
      putJson(`playlist/${playlistName}`, state)
    }
  }

  listenToClick("update_backup", function() {
    saveBackup()
    $.toast("Backup successfully created")
  })
  listenToClick("load_backup", function() {
    const state = JSON.parse(localStorage.getItem(backupKey))
    if (state.songs.length === 0) {
      console.log("Won't load empty backup")
      return
    }
    setState(state)
    Volume.setManualVolume(state.volume)
  })

  const ONE_MINUTE = 60 * 1000
  setInterval(saveBackup, ONE_MINUTE)
})
