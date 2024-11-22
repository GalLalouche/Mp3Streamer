import * as NewAlbumInfo from './new_albums_info.js'
import {Lyrics} from './lyrics.js'
import {External} from './external.js'
import {getDebugAlbum, getDebugSong, isMuted, WAIT_DELAY} from './initialization.js'
import {Globals} from "./globals.js"
import {gplaylist, Playlist, Song} from "./types.js"
import {Volume} from "./volume.js"
import {Score} from "./score.js"
import {Local} from "./local.js"

declare class JPlayerPlaylist extends Playlist {
  add(song: Song, playNow: boolean): void
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
  const randomSongUrl = "data/randomSong"
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

  function jPlayerObject(): any {
    return $(JPLAYER_ID).data('jPlayer')
  }

  const getMedia = () => jPlayerObject().htmlElement.media

  // On play event hook
  // TODO don't call if the same song?
  jPlayerObject().onPlay = function () {
    const currentPlayingSong = playlist.currentPlayingSong()
    const media = getMedia()
    const songInfo = `${currentPlayingSong.artistName} - ${currentPlayingSong.title}`
    Local.setOfflineUrl(currentPlayingSong).then(function () {
      assert(currentPlayingSong.offlineUrl !== undefined)
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
    Volume.setPeak(currentPlayingSong)
    Score.show(currentPlayingSong)
    NewAlbumInfo.show(currentPlayingSong)
  }
  $(isMuted() ? ".jp-mute" : ".jp-volume-max").click()

  function loadNextRandom(playNow: boolean): void {
    $.get(randomSongUrl, function (data) {
      playlist.add(data, playNow)
    })
  }

  const debugStartSong = getDebugSong()
  const debugStartAlbum = getDebugAlbum()
  if (debugStartSong) {
    console.log(`Adding debug song <${debugStartSong}>`)
    $.get("/data/songs/" + debugStartSong, data => gplaylist.add(data, true))
  } else if (debugStartAlbum) {
    console.log(`Adding debug album <${debugStartAlbum}>`)
    // No idea why this is reversed in the playlist :|
    $.get("/data/albums/" + debugStartAlbum, data => gplaylist.add(data.reverse(), true))
  } else
    loadNextRandom(true)
  // Fetches new songs before current song ends.
  setInterval(function () {
    const media = getMedia()
    const isSongNearlyFinished = media.duration - media.currentTime < WAIT_DELAY
    if (shouldLoadNextSongFromRandom() && isSongNearlyFinished)
      loadNextRandom(false)
  }, (WAIT_DELAY - 5) * 1000)
})
