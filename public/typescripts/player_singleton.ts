import {match, P} from "ts-pattern";
import {Duration, Percentage} from "./common_types.js"
import {GuiEvents, PlayerControls, PlayerControlsTopic, Seek} from "./gui_events.js"
import {HtmlPlayer} from "./html_player.js"
import {JPlayerPlaylist} from "./jplayer.playlist.js"
import {Song} from "./media.js"
import * as PlayerGUI from "./player_gui.js"
import {ReplayGainAwareVolume} from "./replay_gain_volume.js";
import {Player, PlayerEvent, Playlist, TimeUpdate, Volume} from "./types.js"

// TODO temporary, until this is refactored to use a proper singleton method.
export let gplayer!: Player
export let gplaylist!: Playlist

class SingletonPlayer extends Player {
  private readonly player: Player
  private readonly volume: ReplayGainAwareVolume = new ReplayGainAwareVolume()

  private constructor(player: Player) {
    super()
    this.player = player
  }
  static from(player: Player): SingletonPlayer {
    const result = new SingletonPlayer(player)
    result.listen((pe: PlayerEvent) => {
      if (pe instanceof TimeUpdate) {
        PlayerGUI.updatePosition({
          current: result.currentTime(),
          total: result.duration()
        })
        PlayerGUI.setIsPlaying()
      }
    })
    result.player.setVolume(result.volume.replayGainAdjustedVolume())
    // TODO these listens should be made elsewhere
    GuiEvents.listen(PlayerControlsTopic, (control: PlayerControls) => {
      match(control)
        .with("play", () => result.playCurrentSong())
        .with("stop", () => result.stop())
        .with("pause", () => result.pause())
        .with(P.instanceOf(Volume), (v: Volume) => result.setVolume(v))
        .with(P.instanceOf(Seek), (s: Seek) => result.skipTo(result.duration().times(s.percentage.zeroToOne())))
        .exhaustive()
    })
    return result
  }
  override clear(): void {
    this.player.clear()
  }
  override currentTime(): Duration {
    return this.player.currentTime()
  }
  override getVolume(): Volume {
    return this.volume.replayGainAdjustedVolume()
  }
  override isPaused(): boolean {
    return this.player.isPaused()
  }
  override load(song: Song): void {
    this.player.load(song)
    this.player.setVolume(this.volume.setPeak(song))
  }
  override percentageOfSongPlayed(): Percentage {
    return this.player.percentageOfSongPlayed()
  }
  override duration(): Duration {
    return this.player.duration()
  }
  override playCurrentSong(): void {
    this.player.playCurrentSong()
  }
  override pause(): void {
    this.player.pause()
    PlayerGUI.setIsStopped()
  }
  override setVolume(v: Volume): void {
    this.player.setVolume(this.volume.setManualVolume(v))
    PlayerGUI.updateVolume(v)
  }
  override skipTo(duration: Duration): void {
    return this.player.skipTo(duration)
  }
  override stop(): void {
    this.player.stop()
    PlayerGUI.setIsStopped()
  }
  override listen(callback: (pe: PlayerEvent) => void): void {
    this.player.listen(callback)
  }
  override unlisten(callback: (pe: PlayerEvent) => void): void {
    this.player.unlisten(callback)
  }
}

function makePlaylist(player: Player): Playlist {
  const pl: JPlayerPlaylist = JPlayerPlaylist.create(player)

  return new class extends Playlist {
    override currentIndex() {return pl.currentIndex()}
    override songs() {return pl.songs()}
    override add(song: Song | readonly Song[], playNow: boolean): Promise<void> {
      return pl.add(song, playNow)
    }
    override _next() {return pl.next()}
    override prev() {return pl.previous()}
    override async clear(): Promise<void> {return pl.setPlaylist([])}
    override play(index: number): Promise<void> { return pl.play(index)}
    override select(index: number): Promise<void> {return pl.select(index)}
    override removeItem(index: number, type: "x" | "up" | "down") {pl.removeItem(index, type)}
  }
}

$(function () {
  gplayer = SingletonPlayer.from(HtmlPlayer.create())
  gplaylist = makePlaylist(gplayer)
})
