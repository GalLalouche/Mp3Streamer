import {match, P} from "ts-pattern";
import {Duration, Percentage} from "./common_types.js"
import {GuiEvents, PlayerControls, PlayerControlsTopic, Seek} from "./gui_events.js"
import {HtmlPlayer} from "./html_player.js"
import {JPlayerPlaylist} from "./jplayer.playlist.js"
import {Song} from "./media.js"
import * as PlayerGUI from "./player_gui.js"
import {Player, PlayerEvent, Playlist, TimeUpdate} from "./types.js"
import {Volume} from "./volume";

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
  override clear(): void {this.player.clear()}
  override currentTime(): Duration {return this.player.currentTime()}
  override getVolume(): Volume {return this.volume.replayGainAdjustedVolume()}
  override isPaused(): boolean {return this.player.isPaused()}
  override load(song: Song): void {
    this.player.load(song)
    this.player.setVolume(this.volume.setPeak(song.trackGain))
  }
  override percentageOfSongPlayed(): Percentage {return this.player.percentageOfSongPlayed()}
  override duration(): Duration {return this.player.duration()}
  override playCurrentSong(): void {this.player.playCurrentSong()}
  override pause(): void {
    this.player.pause()
    PlayerGUI.setIsStopped()
  }
  override setVolume(v: Volume): void {
    this.player.setVolume(this.volume.setManualVolume(v))
    PlayerGUI.updateVolume(v)
  }
  override skipTo(duration: Duration): void {return this.player.skipTo(duration)}
  override stop(): void {
    this.player.stop()
    PlayerGUI.setIsStopped()
  }
  override listen(callback: (pe: PlayerEvent) => void): void {this.player.listen(callback)}
  override unlisten(callback: (pe: PlayerEvent) => void): void {this.player.unlisten(callback)}
}

class ReplayGainAwareVolume {
  private static readonly DEFAULT_GAIN = -10.0

  // The volume that was preset by the user. Start at 20.0, so it could increase 5-fold.
  private volumeBaseline: number = 20.0 // In 0 to 100 units, but can actually pass 100 before scaling.
  private currentGain: number = ReplayGainAwareVolume.DEFAULT_GAIN

  setManualVolume(v: Volume): Volume {
    this.volumeBaseline = v.percentage().zeroToHundred() / this.calculateVolumeCoefficientFromGain()
    return this.replayGainAdjustedVolume()
  }
  setPeak(trackGain: number): Volume {
    this.currentGain = trackGain
    return this.replayGainAdjustedVolume()
  }
  replayGainAdjustedVolume(): Volume {
    const p = this.volumeBaseline * this.calculateVolumeCoefficientFromGain()
    return new Volume(Percentage.fromMax100(Math.min(p, 100)))
  }

  private calculateVolumeCoefficientFromGain(): number {return Math.pow(2, this.currentGain / 10.0)}
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
    override play(index: number): Promise<void> {return pl.play(index)}
    override select(index: number): Promise<void> {return pl.select(index)}
    override removeItem(index: number, type: "x" | "up" | "down") {pl.removeItem(index, type)}
  }
}

$(function () {
  gplayer = SingletonPlayer.from(HtmlPlayer.create())
  gplaylist = makePlaylist(gplayer)
})
