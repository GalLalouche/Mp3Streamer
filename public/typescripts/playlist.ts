import {RemoveType} from "./gui_events.js"
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
  // The list presentation reversed, so song at index 0 is actually the last song, not the first.
  getDisplayedIndex(index: number): number {return this.length() - 1 - index}
  abstract removeItem(index: number, type: RemoveType): void
}
