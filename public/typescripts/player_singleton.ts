import {match, P} from "ts-pattern"
import {Duration, Percentage} from "./common_types.js"
import {GuiEvents, PlayerControls, PlayerControlsTopic, Startup, PlaylistEventTopic, Seek} from "./events.js"
import * as DataApi from "./data_api.js"
import {HtmlPlayer} from "./html_player.js"
import {HtmlPlaylist} from "./html_playlist.js"
import {Song} from "./media.js"
import * as PlayerGUI from "./player_gui.js"
import {PlayerEvent, TimeUpdate} from "./player.js"
import {Player} from "./player.js"
import {Volume} from "./volume.js"
import {Playlist} from "./playlist.js"

// TODO temporary, until this is refactored to use a proper singleton method.
export let gplayer!: Player
export let gplaylist!: Playlist

function globalAux(x: any): any {
  if (isNullable(x))
    throw new Error("gplayer and playlist aren't initialized yet; use GuiEvents.listen(PlayerSetup, ...) instead")
  return x
}
Object.defineProperty(globalThis, "gplayer", {get: () => globalAux(gplayer)})
Object.defineProperty(globalThis, "gplaylist", {get: () => globalAux(gplaylist)})

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
      if (pe instanceof TimeUpdate)
        PlayerGUI.updatePosition({
          current: result.currentTime(),
          total: result.duration(),
        })
      if (pe instanceof Song)
        PlayerGUI.setCurrentSong(pe)
    })
    result.player.setVolume(result.volume.replayGainAdjustedVolume())
    GuiEvents.listen(PlayerControlsTopic, (control: PlayerControls) => {
      match(control)
        .with("play", () => result.playCurrentSong())
        .with("stop", () => result.stop())
        .with("pause", () => result.pause())
        .with(P.instanceOf(Volume), v => result.setVolume(v))
        .with(P.instanceOf(Seek), s => result.skipTo(result.duration().times(s.percentage.zeroToOne())))
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
  override playCurrentSong(): Promise<void> {
    PlayerGUI.setIsPlaying()
    return this.player.playCurrentSong()
  }
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
  const pl: HtmlPlaylist = HtmlPlaylist.create()

  const result = new class extends Playlist {
    override currentIndex() {return pl.currentIndex()}
    override songs() {return pl.songs()}
    override getSong(index: number) {return pl.getSong(index)}
    override length() {return pl.length()}
    override add(song: Song | readonly Song[], playNow: boolean): Promise<void> {
      return pl.add(song, playNow)
    }
    override async next(): Promise<void> {
      if (pl.next().isFalse()) {
        await addNextRandomSong()
        return this.next()
      }
    }
    override prev() {return pl.previous()}
    override clear(): Promise<void> {return pl.setPlaylist([])}
    override async select(index: number): Promise<void> {
      // This check is here since the JPlayerPlaylist actually does need to support selecting the
      // current song (for some reason). FIXME Probably a bug, should be fixed.
      if (pl.currentIndex() !== index)
        await pl.select(index)
    }
  }
  GuiEvents.listen(PlaylistEventTopic, event => {
    match(event)
      .with('next', () => result.next())
      .with('previous', () => result.prev())
      .exhaustive()
  })
  // Can't use ts-pattern here since Song's constructor is private
  async function maybePreloadNextSong(tu: TimeUpdate): Promise<void> {
    if (pl.isLastSongPlaying() && PRELOAD_GAP.isGreaterThanOrEqual(tu.remainingDuration()))
      await addNextRandomSong()
  }
  async function addNextRandomSong(): Promise<void> {
    return DataApi.getRandomSong().then(song => result.add(song, false))
  }
  player.listen((e: PlayerEvent) => {

    if (e === "ENDED")
      result.next()
    else if (e instanceof TimeUpdate)
      maybePreloadNextSong(e)
    else
      // Songs are changed *via* the playlist
      assert(result.getSong(result.currentIndex()) === e)
  })
  return result
}

const PRELOAD_GAP = Duration.fromSeconds(20)

$(function () {
  gplayer = SingletonPlayer.from(HtmlPlayer.create())
  gplaylist = makePlaylist(gplayer)
  Startup.resolve({player: gplayer, playlist: gplaylist})
})
