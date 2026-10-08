import {Duration, Percentage} from "./common_types.js"
import {Song} from "./media.js"
import {Volume} from "./volume.js"

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


