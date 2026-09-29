import {Duration, Percentage, Player, PlayerEvent, Song, TimeUpdate, Volume} from "./types.js";
import {PlayerGUI} from "./player_gui.js";

/** Implements the Player interface using a hidden HTML5 audio element. */
export class PlayerImpl extends Player {
  private readonly html: HTMLAudioElement
  constructor() {
    super()
    this.html = document.createElement("audio")
  }

  // startGuiUpdates(): void {
  //   this.html.ontimeupdate =
  //     () => PlayerGUI.updatePosition(this.currentPlayingInSeconds(), this.getDuration())
  //   this.html.onpause = () => PlayerGUI.setIsStopped()
  //   this.html.onplay = () => PlayerGUI.setIsPlaying()
  //   this.html.onvolumechange = () => PlayerGUI.updateVolume(this.getVolume())
  // }
  //
  // stopGuiUpdates(): void {
  //   this.html.ontimeupdate = null
  //   this.html.onpause = null
  //   this.html.onplay = null
  //   this.html.onvolumechange = null
  // }

  override currentTime(): Duration {return Duration.fromSeconds(this.html.currentTime)}
  override duration(): Duration {return Duration.fromSeconds(this.html.duration)}
  override isPaused(): boolean {return this.html.paused}
  // TODO should this be async to signify when done?
  override load(song: Song): void {
    PlayerGUI.setCurrentSong(song)
    this.html.src = song.offlineUrl!! // FIXME this just assume the offline URL is already set
  }
  override pause(): void {
    this.html.pause()
  }
  override percentageOfSongPlayed(): Percentage {
    return isNaN(this.html.duration) ?
      Percentage.fromMax100(0) :
      Percentage.fromMax1(this.currentTime().toSeconds() / this.duration().toSeconds())
  }
  override playCurrentSong(): void {
    // TODO should this return a promise as well?
    this.html.play()
  }
  override getVolume(): Volume {
    return Volume.fromPercentage(Percentage.fromMax1(this.html.volume))
  }
  override setVolume(v: Volume): void {
    v.setVolume(this.html)
    PlayerGUI.updateVolume(v)
  }
  override skipTo(duration: Duration): void {
    this.html.currentTime = duration.toSeconds()
  }
  override stop(): void {
    this.html.pause()
    this.html.currentTime = 0
  }
  override clear(): void {
    this.stop()
    this.html.src = ""
  }
  private getDuration() {
    return Duration.fromSeconds(this.html.duration)
  }

  listen(callback: (pe: PlayerEvent) => void): void {
    this.html.ontimeupdate = () =>
      callback(new TimeUpdate({
        currentDuration: this.currentTime(),
        totalDuration: this.getDuration()
      }))
    this.html.onended = () => callback("ENDED")
  }
}