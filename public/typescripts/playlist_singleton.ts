import {match} from "ts-pattern"
import {Events, PlaylistEventTopic} from "./events.js"
import {Duration} from "./common_types.js"
import * as DataApi from "./data_api.js"
import * as Local from "./local.js"
import {HtmlPlaylist} from "./html_playlist.js"
import {Song} from "./media.js"
import {PlayerEvent, TimeUpdate} from "./player.js"
import {Player} from "./player.js"
import {Playlist} from "./playlist.js"

export function makePlaylist(player: Player): Playlist {
  const pl: HtmlPlaylist = HtmlPlaylist.create()

  const result = new class extends Playlist {
    override currentIndex() {return pl.currentIndex()}
    override songs() {return pl.songs()}
    override getSong(index: number) {return pl.getSong(index)}
    override length() {return pl.length()}
    override async add(song: Song | readonly Song[]): Promise<number> {
      const lastIndex = () => pl.length() - 1
      if (song instanceof Song) {
        await Local.maybePreLoad(song)
        pl.add(song)
        return lastIndex()
      }

      let current = null
      for (const s of song) {
        await this.add(s)
        if (current === null)
          current = lastIndex()
      }
      return current ?? lastIndex()
    }
    override async next(): Promise<void> {
      if (pl.next().isFalse()) {
        await addNextRandomSong()
        return this.next()
      }
    }
    override prev() {return pl.previous()}
    override clear(): Promise<void> {return pl.setPlaylist([])}
    override async select(index: number): Promise<void> {
      await Local.maybePreLoad(pl.getSong(index))
      pl.select(index)
    }
  }

  Events.listen(PlaylistEventTopic, event => {
    match(event)
      .with('next', () => result.next())
      .with('previous', () => result.prev())
  })

  async function maybePreloadNextSong(tu: TimeUpdate): Promise<void> {
    if (pl.isLastSongPlaying() && PRELOAD_GAP.isGreaterThanOrEqual(tu.remainingDuration()))
      await addNextRandomSong()
  }
  async function addNextRandomSong(): Promise<void> {
    return DataApi.getRandomSong().then(song => result.add(song)).void()
  }
  player.listen((e: PlayerEvent) => {
    // Can't use ts-pattern here since Song's constructor is private
    if (e === "ENDED")
      result.next()
    else if (e instanceof TimeUpdate)
      maybePreloadNextSong(e)
    else
      // Songs are changed *via* the playlist
      assert(result.getSong(result.currentIndex()) === e)
  })
  return result
}

const PRELOAD_GAP = Duration.fromSeconds(20)
