import {HtmlPlaylist} from "./html_playlist.js"
import * as Local from "./local.js"
import {Song} from "./media.js"
import {Playlist} from "./playlist.js"

export function makePlaylist(): Playlist {
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
      pl.next()
    }
    override prev() {return pl.previous()}
    override clear(): Promise<void> {return pl.setPlaylist([])}
    override async select(index: number): Promise<void> {
      await Local.maybePreLoad(pl.getSong(index))
      pl.select(index)
    }
  }

  return result
}
