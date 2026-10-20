/** Processes events and wires them to the playlist. */
import * as DataApi from "./data_api.js"
import {match, P} from "ts-pattern"
import {Events, PlayerEvent, PlayerEvents, PlaylistEvents, TimeUpdate} from "./events.js"
import {Duration} from "./common_types.js"
import {Song} from "./media.js"
import {Playlist} from "./playlist.js"
import {addAndFade} from "./globals.js"

export function wire(playlist: Playlist) {
  Events.listen(PlayerEvents, (e: PlayerEvent) => {
    match(e)
      .with("ENDED", () => playlist.next())
      .with(P.instanceOf(TimeUpdate), tu => maybePreloadNextSong(tu))
      // Songs are changed *via* the playlist
      .with(P.instanceOf(Song), s => playlist.getSong(playlist.currentIndex()) === s)
      .exhaustive()
  })

  Events.listen(PlaylistEvents, event => {
    match(event)
      .with('next', async () => {
        if (playlist.isLastSongPlaying())
          await addNextRandomSong({fade: false})
        playlist.next()
      })
      .with('previous', () => playlist.prev())
      .exhaustive()
  })

  async function maybePreloadNextSong(tu: TimeUpdate): Promise<void> {
    if (playlist.isLastSongPlaying() && PRELOAD_GAP.isGreaterThanOrEqual(tu.remainingDuration()))
      // FIXME should only fade if the current song isn't the last one in a chain too.
      await addNextRandomSong({fade: true})
  }
  async function addNextRandomSong(arg: {fade: boolean}): Promise<void> {
    return DataApi.getRandomSong().then(song => arg.fade ? addAndFade(song) : playlist.add(song).void())
  }
}

const PRELOAD_GAP = Duration.fromSeconds(400)
