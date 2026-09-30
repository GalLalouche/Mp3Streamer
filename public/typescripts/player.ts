import {Duration, Percentage, Player, PlayerEvent, Song, TimeUpdate, Volume} from "./types.js";

/** Implements the Player interface using a hidden HTML5 audio element. */
export class PlayerImpl extends Player {
  listeners: ((pe: PlayerEvent) => void)[] = []
  private readonly html: HTMLAudioElement
  private song?: Song
  private constructor() {
    super()
    this.html = document.createElement("audio")
  }

  static create(): PlayerImpl {
    const result = new PlayerImpl()
    result.startGuiUpdates()
    return result
  }

  startGuiUpdates(): void {
    const that = this
    this.html.ontimeupdate = () => that.publish(new TimeUpdate({
      currentDuration: that.currentTime(),
      totalDuration: that.duration()
    }))
    // this.html.onpause = () => that.maybePublish(new PlayerEvent("pause"))
    // this.html.onplay = () => PlayerGUI.setIsPlaying()
    // this.html.onvolumechange = () => PlayerGUI.updateVolume(this.getVolume())
    this.html.onended = () => that.publish("ENDED")
  }

  publish(pe: PlayerEvent): void {
    for (const listener of this.listeners)
      listener(pe)
  }
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
    // PlayerGUI.setCurrentSong(song)
    this.html.src = song.offlineUrl!! // FIXME this just assume the offline URL is already set
    this.song = song
    this.publish(song)
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
    // PlayerGUI.updateVolume(v)
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

  override listen(callback: (pe: PlayerEvent) => void): void {
    this.listeners.push(callback)
  }
  override unlisten(callback: (pe: PlayerEvent) => void): void {
    this.listeners = this.listeners.filter(cb => cb !== callback)
  }
}