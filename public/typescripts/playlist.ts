import {RemoveType} from "./events.js"
import {Song} from "./media.js"

export abstract class Playlist {
  abstract clear(instant: boolean): Promise<void>
  async setPlaylist(playlist: readonly Song[], instant: boolean): Promise<void> {
    await this.clear(instant)
    for (const s of playlist)
      await this.add(s, false)
  }
  abstract add(song: Song | readonly Song[], playNow: boolean): Promise<void>
  abstract next(): Promise<void>
  abstract select(index: number): Promise<void>
  abstract prev(): void
  abstract currentIndex(): number
  /** Throws on invalid index. */
  abstract getSong(index: number): Song
  currentPlayingSong(): Song {return this.getSong(this.currentIndex())}
  abstract songs(): readonly Song[]
  last(): Song {return this.getSong(this.length() - 1)}
  abstract length(): number
  abstract removeItem(index: number, type: RemoveType): void
}
