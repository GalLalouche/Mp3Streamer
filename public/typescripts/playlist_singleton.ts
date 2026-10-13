import {match} from "ts-pattern"
import {GuiEvents, PlaylistEventTopic} from "./events.js"
import * as DataApi from "./data_api.js"
import {HtmlPlaylist} from "./html_playlist.js"
import {Song} from "./media.js"
import {PlayerEvent, TimeUpdate} from "./player.js"
import {Player} from "./player.js"
import {Playlist} from "./playlist.js"
import {Duration} from "./common_types.js"

export function makePlaylist(player: Player): Playlist {
  const pl: HtmlPlaylist = HtmlPlaylist.create()

  const result = new class extends Playlist {
    override currentIndex() {return pl.currentIndex()}
    override songs() {return pl.songs()}
    override getSong(index: number) {return pl.getSong(index)}
    override length() {return pl.length()}
    override add(song: Song | readonly Song[], playNow: boolean): Promise<void> {
      return pl.add(song, playNow)
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
      // This check is here since the JPlayerPlaylist actually does need to support selecting the
      // current song (for some reason). FIXME Probably a bug, should be fixed.
      if (pl.currentIndex() !== index)
        await pl.select(index)
    }
  }
  GuiEvents.listen(PlaylistEventTopic, event => {
    match(event)
      .with('next', () => result.next())
      .with('previous', () => result.prev())
      .exhaustive()
  })
  // Can't use ts-pattern here since Song's constructor is private
  async function maybePreloadNextSong(tu: TimeUpdate): Promise<void> {
    if (pl.isLastSongPlaying() && PRELOAD_GAP.isGreaterThanOrEqual(tu.remainingDuration()))
      await addNextRandomSong()
  }
  async function addNextRandomSong(): Promise<void> {
    return DataApi.getRandomSong().then(song => result.add(song, false))
  }
  player.listen((e: PlayerEvent) => {

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
