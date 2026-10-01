import * as DataApi from "./data_api.js";
import {External} from './external.js'
import {getDebugAlbum, getDebugSong, isMuted} from './initialization.js'
import {Local} from "./local.js"
import {Lyrics} from './lyrics.js'
import * as NewAlbumInfo from './new_albums_info.js'
import {EventForPlaylist, EventsForPlaylist, gplayer, gplaylist} from "./player_singleton.js";
import {Poster} from "./poster.js";
import {Score} from "./score.js"
import {Duration, PlayerEvent, Song, TimeUpdate} from "./types.js"
import {VolumeSetter} from "./volume_setter.js"

// TODO this entire file should split into jplayer specific hacks and more general code

const WAIT_DELAY: Duration = Duration.fromSeconds(25)

// declare class JPlayerPlaylist extends Playlist {
//   add(song: Song, playNow: boolean): Promise<void>
//   protected _next(): void
//   play(index: number): Promise<void>
//   select(index: number): Promise<void>
//   prev(): void
//   currentIndex(): number
//   songs(): Song[]
//   removeItemAux(index: any, next: (x: JQuery<HTMLElement>) => JQuery<HTMLElement>): void
//
//   constructor(
//     cssSelector: { jPlayer: string, cssSelectorAncestor: string },
//     playlist: Song[],
//     options: {
//       swfPath: string,
//       supplied: string,
//     },
//   )
// }

interface PlaylistHacks {
  oldNext: () => void
  next: () => void
}

$(function () {
  const JPLAYER_ID = "#jquery_jplayer_1"
  // const playlist = new JPlayerPlaylist({
  //   jPlayer: JPLAYER_ID,
  //   cssSelectorAncestor: "#jp_container_1",
  // }, [], {
  //   swfPath: "../js",
  //   supplied: "webmv, ogv, m4a, oga, mp3, flac",
  // })
  // Globals.playlist = playlist
  // Modify next to fetch a random song if in shuffle mode and at the last song
  // TODO move to playlist_customization
  let hacks = gplaylist as any
  hacks.oldNext = hacks.next
  const shouldLoadNextSongFromRandom = () => gplaylist.isLastSongPlaying()
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
        if ($(JPLAYER_ID).data('jPlayer')) {
          const media = $(JPLAYER_ID).data('jPlayer').htmlElement.media
          if (currentPlayingSong.file === gplaylist.currentPlayingSong().file && media && media.offlineUrl === undefined)
            media.offlineUrl = currentPlayingSong.offlineUrl
        }
      })
      $(".jp-currently-playing").html(songInfo)
      document.title = songInfo
      $('#favicon').remove()

      // TODO use plain old observers here
      // FIXME leftover of the old architecture.
      Lyrics.show(currentPlayingSong)
      External.show(currentPlayingSong)
      VolumeSetter.setPeak(currentPlayingSong)
      Score.show(currentPlayingSong)
      Poster.setImage(currentPlayingSong.poster)
      $('head')
        .append(`<link href="${($("img.poster")[0] as any).src}" id="favicon" rel="shortcut icon">`)
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
  if (gplayer)
    setup()
  else
    $(document).on(EventsForPlaylist, e => {
      let event = e as unknown as CustomEvent<EventForPlaylist>
      if (event.detail != "READY") {
        return
      }
      setup()
    })

  function setup(): void {
    if (debugStartSong) {
      console.log(`Adding debug song <${debugStartSong}>`)
      DataApi.getSong(debugStartSong).then(data => gplaylist.add(data, true))
    } else if (debugStartAlbum) {
      console.log(`Adding debug album <${debugStartAlbum}>`)
      // No idea why this is reversed in the playlist :|
      DataApi.getAlbum("/data/albums/" + debugStartAlbum).then(data => gplaylist.add(data.reverse(), true))
    } else
      loadNextRandom(true)
  }

  function loadNextRandom(playNow: boolean): void {
    DataApi.getRandomSong().then(song => gplaylist.add(song, playNow))
  }
})
