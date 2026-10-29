import {Duration, Percentage} from "./common_types.js"
import {PlayerEvent} from "./events.js"
import {Song} from "./media.js"
import {Volume} from "./volume.js"

export type PlayerEventListener = (pe: PlayerEvent) => void

export abstract class Player {
  abstract load(song: Song): void
  abstract playCurrentSong(): Promise<void>
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
  /** Listen to this player specifically (as opposed to the global Events PubSub. */
  abstract listen(callback: PlayerEventListener): void
  abstract unlisten(callback: PlayerEventListener): void
  hasEnded(): boolean {return this.currentTime().equals(this.duration())}
}

