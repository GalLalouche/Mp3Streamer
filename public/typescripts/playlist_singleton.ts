import {HtmlPlaylist} from "./html_playlist.js"
import * as Local from "./local.js"
import {Song} from "./media.js"
import {Playlist} from "./playlist.js"

export function makePlaylist(): Playlist {
  const pl: HtmlPlaylist = HtmlPlaylist.create()

  return new class extends Playlist {
    override currentIndex() {return pl.currentIndex()}
    override songs() {return pl.songs()}
    override getSong(index: number) {return pl.getSong(index)}
    override length() {return pl.length()}
    override async add(song: Song | readonly Song[]): Promise<number> {
      const lastIndex = () => pl.length() - 1
      if (song instanceof Song) {
        await Local.maybeDownloadLocally(song)
        pl.add(song, 'animate')
        return lastIndex()
      }

      let current: number | null = null
      for (const s of song) {
        pl.add(s, 'instant')
        current = current ?? lastIndex()
      }
      return current ?? lastIndex()
    }
    override next() {pl.next()}
    override prev() {pl.previous()}
    override clear() {pl.setPlaylist([])}
    override async select(index: number): Promise<void> {
      await Local.maybeDownloadLocally(pl.getSong(index))
      pl.select(index)
    }
  }
}
