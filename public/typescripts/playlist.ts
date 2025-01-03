import {PLAYLIST_NAME_KEY, Poster} from "./poster.js"
import {gplayer, gplaylist, Song} from "./types.js"
import {Volume} from "./volume.js"

$(function () {
  class PlaylistJson {
    constructor(
      public songs: Song[],
      public currentIndex: number,
      public duration: number,
      public volume: number,
    ) {}
  }

  const body = $("body")

  function listenToClick(id: string, callback: () => void): void {
    body.on("click", "button#" + id, callback)
  }

  listenToClick("load_playlist", () => $.get("playlist/", ids => chooseState(ids)))

  function chooseState(ids: string[]): void {
    // Create the dialog div
    const $dialog = div({id: 'dialog', title: 'Select a playlist'})

    for (const id of ids) {
      $dialog.append($('<button>', {
        text: id,
        click: async () => {
          await loadPlaylist(id)
          $dialog.dialog("close")
        },
      })).appendBr()
    }

    $dialog.dialog({autoOpen: true, modal: true})
    $dialog.on('dialogclose', () => $dialog.remove())
    $dialog.dialog("open")
  }

  async function loadPlaylist(id: string): Promise<void> {
    $.get("playlist/" + id, async function (playlist: PlaylistJson) {
      await setState(playlist)
    })
  }

  function getState(): PlaylistJson {
    // We don't reset offline_url here, since it could still be used elsewhere, and we wish to
    // avoid cloning the entire song structure.
    return new PlaylistJson(
      gplaylist.songs(),
      gplaylist.currentIndex(),
      gplayer.currentPlayingInSeconds(),
      Volume.getVolumeBaseline(),
    )
  }

  async function setState(state: PlaylistJson): Promise<void> {
    state.songs.forEach(song => song.offlineUrl = undefined)
    gplayer.stop()
    await gplaylist.setPlaylist(state.songs, false)
    await gplaylist.select(state.currentIndex)
    gplayer.skip(state.duration)
    Volume.setManualVolume(state.volume)
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
    const playlistName = Poster.playlistName.val() as string
    if (playlistName) {
      localStorage.setItem(PLAYLIST_NAME_KEY, playlistName)
      console.log(`Saving playlist ${playlistName} remotely`)
      putJson(`playlist/${playlistName}`, state)
    }
  }

  listenToClick("update_backup", function () {
    saveBackup()
    $.toast("Backup successfully created")
  })
  listenToClick("load_backup", async function () {
    const item = localStorage.getItem(backupKey)
    if (!item) {
      $.toast("No backup to load!")
      return
    }
    const state = JSON.parse(item)
    if (state.songs.length === 0) {
      console.log("Won't load empty backup")
      return
    }
    await setState(state)
  })

  const ONE_MINUTE = 60 * 1000
  setInterval(saveBackup, ONE_MINUTE)
})
