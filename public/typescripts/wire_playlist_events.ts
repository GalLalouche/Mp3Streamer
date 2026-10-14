/** Processes events and wires them to the playlist. */
import * as DataApi from "./data_api.js"
import {match} from "ts-pattern"
import {Events, PlayerEvent, PlayerEvents, PlaylistEventTopic, Startup, TimeUpdate} from "./events.js"
import {Duration} from "./common_types.js"

$(function () {
  Startup.promise.then(async ({playlist}) => {
    Events.listen(PlayerEvents, (e: PlayerEvent) => {
      // Can't use ts-pattern here since Song's constructor is private
      if (e === "ENDED")
        playlist.next()
      else if (e instanceof TimeUpdate)
        maybePreloadNextSong(e)
      else
        // Songs are changed *via* the playlist
        assert(playlist.getSong(playlist.currentIndex()) === e)
    })

    Events.listen(PlaylistEventTopic, event => {
      match(event)
        .with('next', async () => {
          if (playlist.isLastSongPlaying())
            await addNextRandomSong()
          playlist.next()
        })
        .with('previous', () => playlist.prev())
    })

    async function maybePreloadNextSong(tu: TimeUpdate): Promise<void> {
      if (playlist.isLastSongPlaying() && PRELOAD_GAP.isGreaterThanOrEqual(tu.remainingDuration()))
        await addNextRandomSong()
    }
    async function addNextRandomSong(): Promise<void> {
      return DataApi.getRandomSong().then(song => playlist.add(song)).void()
    }
  })
})

const PRELOAD_GAP = Duration.fromSeconds(20)
