/**
 * Code related to loading and saving the playlist remotely or locally. If you want code related
 * to the actual playlist, you probably want types.ts, playlist_customization.ts, or
 * jplayer.playlist.ts.
 */

import './jquery_common_xhr.js'
import * as API from "./api.js";
import {Duration} from "./common_types.js";
import {isMuted} from "./initialization.js";
import {Song} from "./media.js";
import {gplayer, gplaylist} from "./player_singleton.js";
import * as Poster from "./poster.js"
import {Volume} from "./types.js"
import * as VolumeSetter from "./volume_setter.js"

$(function () {
    class PlaylistJson {
      constructor(
        public songs: readonly Song[],
        public currentIndex: number,
        public duration: Duration,
        public volume: Volume,
      ) {}

      static fromJson(json: API.RawJSON): PlaylistJson {
        return new PlaylistJson(
          json.songs.map(Song.fromJSON),
          json.currentIndex as number,
          Duration.fromJson(json.duration),
          Volume.fromJSON(json.volume),
        )
      }
    }

    const body = $("body")

    function listenToClick(id: string, callback: () => void): void {
      body.on("click", "button#" + id, callback)
    }

    listenToClick("load_playlist", () => getPlaylists().then(ids => chooseState(ids)))

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

    async function getPlaylist(id: string): Promise<PlaylistJson> {
      return API.get("playlist/" + id).then(PlaylistJson.fromJson)
    }

    async function getPlaylists(): Promise<string[]> {
      return API.get("playlists/").then(e => e as string[])
    }

    async function loadPlaylist(id: string): Promise<void> {
      return getPlaylist(id).then(setState)
    }

    function getState(): PlaylistJson {
      // We don't reset offline_url here, since it could still be used elsewhere, and we wish to
      // avoid cloning the entire song structure.
      return new PlaylistJson(
        gplaylist.songs(),
        gplaylist.currentIndex(),
        gplayer.currentTime(),
        VolumeSetter.getVolumeBaseline(),
      )
    }

    async function setState(state: PlaylistJson): Promise<void> {
      state.songs.forEach(song => song.offlineUrl = undefined)
      gplayer.stop()
      await gplaylist.setPlaylist(state.songs, false)
      await gplaylist.select(state.currentIndex)
      gplayer.skipTo(state.duration)
      VolumeSetter.setManualVolume(state.volume)
      // gplayer.playCurrentSong()
    }

    const backupKey = "backup"

    // If there was an error, returns a toastOptions with the error.
    async function saveBackup(toastErrors: boolean): Promise<toastOptions | void> {
      const state = getState()
      if (state.songs.length <= 1) {
        const msg = "Won't save trivial backup"
        console.log(msg)
        return Promise.resolve({heading: 'Warning', text: msg, icon: 'warning'})
      }
      state.volume = VolumeSetter.getVolumeBaseline()
      localStorage.setItem(backupKey, JSON.stringify(state))
      const playlistName = Poster.playlistName.val() as string
      if (playlistName) {
        localStorage.setItem(Poster.PLAYLIST_NAME_KEY, playlistName)
        console.log(`Saving playlist ${playlistName} remotely`)
        return putJson(`playlist/${playlistName}`, state)
          .toPromise()
          .void()
          .catch(
            (error: any) => {
              const result: toastOptions = {
                heading: 'Error while saving backup',
                text: error.statusText,
                icon: 'error',
                hideAfter: 10_000,
              }
              if (toastErrors)
                $.toast(result)
              return Promise.resolve(result)
            })
      }
      return Promise.resolve()
    }

    listenToClick("update_backup", async function () {
      const result = await saveBackup(false)
      if (result) {
        $.toast(result)
      } else {
        $.toast("Backup successfully created")
      }
    })
    listenToClick("load_backup", async function () {
      const item = localStorage.getItem(backupKey)
      if (!item) {
        $.toast("No backup to load!")
        return
      }
      const state = PlaylistJson.fromJson(JSON.parse(item))
      if (state.songs.length === 0) {
        console.log("Won't load empty backup")
        return
      }
      await setState(state)
    })

    const ONE_MINUTE = 60 * 1000
    if (isMuted().isFalse()) {
      setInterval(() => saveBackup(true), ONE_MINUTE)
    }
  },
)
