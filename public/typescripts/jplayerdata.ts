import * as NewAlbumInfo from './new_albums_info.js'
import {Lyrics} from './lyrics.js'
import {External} from './external.js'
import {getDebugAlbum, getDebugSong, isMuted} from './initialization.js'
import {Globals} from "./globals.js"
import {Duration, gplaylist, PlayerEvent, Playlist, Song, TimeUpdate} from "./types.js"
import {VolumeSetter} from "./volume_setter.js"
import {Score} from "./score.js"
import {Local} from "./local.js"
import * as DataApi from "./data_api.js";
import {EventForPlaylist, EventsForPlaylist, gplayer} from "./player_singleton.js";

// TODO this entire file should split into jplayer specific hacks and more general code

const WAIT_DELAY: Duration = Duration.fromSeconds(25)

declare class JPlayerPlaylist extends Playlist {
  add(song: Song, playNow: boolean): Promise<void>
  protected _next(): void
  play(index: number): Promise<void>
  select(index: number): Promise<void>
  prev(): void
  currentIndex(): number
  songs(): Song[]

  constructor(
    cssSelector: { jPlayer: string, cssSelectorAncestor: string },
    playlist: Song[],
    options: {
      swfPath: string,
      supplied: string,
    },
  )
}

interface PlaylistHacks {
  oldNext: () => void
  next: () => void
}

$(function () {
  const JPLAYER_ID = "#jquery_jplayer_1"
  const playlist = new JPlayerPlaylist({
    jPlayer: JPLAYER_ID,
    cssSelectorAncestor: "#jp_container_1",
  }, [], {
    swfPath: "../js",
    supplied: "webmv, ogv, m4a, oga, mp3, flac",
  })
  Globals.playlist = playlist
  // Modify next to fetch a random song if in shuffle mode and at the last song
  // TODO move to playlist_customization
  let hacks = playlist as unknown as PlaylistHacks
  hacks.oldNext = playlist.next
  const shouldLoadNextSongFromRandom = () => playlist.isLastSongPlaying()
  hacks.next = function () {
    if (shouldLoadNextSongFromRandom())
      loadNextRandom(true)
    else
      hacks.oldNext()
  }

  // On play event hook
  gplayer.listen(function (event: PlayerEvent) {
    if (event instanceof Song) {
      const currentPlayingSong = event
      const songInfo = `${currentPlayingSong.artistName} - ${currentPlayingSong.title}`
      Local.setOfflineUrl(currentPlayingSong).then(function () {
        assert(currentPlayingSong.offlineUrl !== undefined)
        // jplayer hack: update the offlineUrl for the media object
        const media = $(JPLAYER_ID).data('jPlayer').htmlElement.media
        if (currentPlayingSong.file === playlist.currentPlayingSong().file && media && media.offlineUrl === undefined)
          media.offlineUrl = currentPlayingSong.offlineUrl
      })
      $(".jp-currently-playing").html(songInfo)
      document.title = songInfo
      $('#favicon').remove()

      $('head')
        .append(`<link href="${($("img.poster")[0] as any).src}" id="favicon" rel="shortcut icon">`)

      // TODO use plain old observers here
      Lyrics.show(currentPlayingSong)
      External.show(currentPlayingSong)
      VolumeSetter.setPeak(currentPlayingSong)
      Score.show(currentPlayingSong)
      NewAlbumInfo.show(currentPlayingSong)
    } else if (event instanceof TimeUpdate) {
      // Fetches new songs before current song ends.
      const isSongNearlyFinished = WAIT_DELAY.isGreaterThanOrEqual(event.totalDuration.minus(event.currentDuration))
      if (shouldLoadNextSongFromRandom() && isSongNearlyFinished)
        loadNextRandom(false)
    }
  })

  const debugStartSong = getDebugSong()
  const debugStartAlbum = getDebugAlbum()

  $(isMuted() ? ".jp-mute" : ".jp-volume-max").click()
  $(document).on(EventsForPlaylist, e => {
    let event = e as unknown as CustomEvent<EventForPlaylist>
    if (event.detail != "READY") {
      return
    }
    if (debugStartSong) {
      console.log(`Adding debug song <${debugStartSong}>`)
      DataApi.getSong(debugStartSong).then(data => gplaylist.add(data, true))
    } else if (debugStartAlbum) {
      console.log(`Adding debug album <${debugStartAlbum}>`)
      // No idea why this is reversed in the playlist :|
      DataApi.getAlbum("/data/albums/" + debugStartAlbum).then(data => gplaylist.add(data.reverse(), true))
    } else
      loadNextRandom(true)
  })

  function loadNextRandom(playNow: boolean): void {
    DataApi.getRandomSong().then(song => playlist.add(song, playNow))
  }
})
