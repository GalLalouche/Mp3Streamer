import {Song} from "./media.js"

export abstract class Playlist {
  abstract clear(instant: boolean): Promise<void>
  async setPlaylist(playlist: readonly Song[], instant: boolean): Promise<void> {
    await this.clear(instant)
    await this.add(playlist)
  }
  /** Returns the index of the *first* song added. */
  abstract add(song: Song | readonly Song[]): Promise<number>
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
  isLastSongPlaying() {return this.currentIndex() === this.length() - 1}
}
