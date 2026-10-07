import {Duration, Percentage} from "./common_types.js"
import {Song} from "./media.js"
import {Volume} from "./volume.js";

export class TimeUpdate {
  readonly currentDuration: Duration
  readonly totalDuration: Duration

  constructor(input: { currentDuration: Duration, totalDuration: Duration }) {
    this.currentDuration = input.currentDuration
    this.totalDuration = input.totalDuration
  }
}

export type PlayerEvent = "ENDED" | TimeUpdate | Song
export type PlayerEventListener = (pe: PlayerEvent) => void

export abstract class Player {
  abstract load(song: Song): void
  abstract playCurrentSong(): void
  abstract stop(): void
  /** Does not unpause. */
  abstract pause(): void
  abstract isPaused(): boolean
  togglePause(): void {
    if (this.isPaused())
      this.playCurrentSong()
    else
      this.pause()
  }
  abstract percentageOfSongPlayed(): Percentage
  abstract duration(): Duration
  abstract currentTime(): Duration
  abstract setVolume(v: Volume): void
  abstract getVolume(): Volume
  abstract skipTo(duration: Duration): void
  abstract clear(): void
  abstract listen(callback: PlayerEventListener): void
  abstract unlisten(callback: PlayerEventListener): void
}

export abstract class Playlist {
  abstract clear(instant: boolean): Promise<void>
  async setPlaylist(playlist: readonly Song[], instant: boolean): Promise<void> {
    await this.clear(instant)
    for (const s of playlist) {
      await this.add(s, false)
    }
  }
  abstract add(song: Song | readonly Song[], playNow: boolean): Promise<void>
  protected abstract _next(): void
  next(count?: number): void {
    count = count || 1
    for (let i = 0; i < count; i++)
      this._next()
  }
  abstract play(index: number): Promise<void>
  abstract select(index: number): Promise<void>
  abstract prev(): void
  abstract currentIndex(): number
  currentPlayingSong(): Song {return this.songs()[this.currentIndex()]}
  abstract songs(): readonly Song[]
  last(): Song {return this.songs()[this.length() - 1]}
  length(): number {return this.songs().length}
  // The list presentation reversed, so song at index 0 is actually the last song, not the first.
  getDisplayedIndex(index: number): number {return this.length() - 1 - index}
  isLastSongPlaying(): boolean {return this.currentIndex() == this.length() - 1}
  // FIXME duplication of remove type between here and events
  abstract removeItem(index: number, type: "x" | "up" | "down"): void
}

