import {Duration, Percentage} from "./common_types.js"
import {Song} from "./media.js"
import {Player, PlayerEvent, PlayerEventListener, TimeUpdate} from "./types.js"
import {Volume} from "./volume.js"

/** Implements the Player interface using an HTML5 audio element. */
export class HtmlPlayer extends Player {
  listeners: PlayerEventListener[] = []
  private readonly html: HTMLAudioElement
  private constructor() {
    super()
    this.html = document.createElement("audio")
  }

  static create(): HtmlPlayer {
    const result = new HtmlPlayer()
    result.html.ontimeupdate = () => result.publish(new TimeUpdate({
      currentDuration: result.currentTime(),
      totalDuration: result.duration(),
    }))
    result.html.onended = () => result.publish("ENDED")
    return result
  }

  private publish(pe: PlayerEvent): void {
    for (const listener of this.listeners)
      listener(pe)
  }

  override currentTime(): Duration {return Duration.fromSeconds(this.html.currentTime)}
  override duration(): Duration {return Duration.fromSeconds(this.html.duration)}
  override isPaused(): boolean {return this.html.paused}
  // TODO should this be async to signify when done?
  override load(song: Song): void {
    // PlayerGUI.setCurrentSong(song)
    this.html.src = song.offlineUrl!! // FIXME this just assume the offline URL is already set
    this.publish(song)
  }
  override pause(): void {this.html.pause()}
  override percentageOfSongPlayed(): Percentage {
    return isNaN(this.html.duration) ?
      Percentage.fromMax100(0) :
      Percentage.fromMax1(this.currentTime().toSeconds() / this.duration().toSeconds())
  }
  override playCurrentSong(): void {
    // TODO should this return a promise as well?
    this.html.play()
  }
  override getVolume(): Volume {return new Volume(Percentage.fromMax1(this.html.volume))}
  override setVolume(v: Volume): void {this.html.volume = v.percentage().zeroToOne()}
  override skipTo(duration: Duration): void {this.html.currentTime = duration.toSeconds()}
  override stop(): void {
    this.html.pause()
    this.html.currentTime = 0
  }
  override clear(): void {
    this.stop()
    this.html.src = ""
  }
  override listen(callback: PlayerEventListener): void {this.listeners.push(callback)}
  override unlisten(callback: PlayerEventListener): void {
    this.listeners = this.listeners.filter(cb => cb !== callback)
  }
}