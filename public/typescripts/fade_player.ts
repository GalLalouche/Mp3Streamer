import {Song} from "./media.js"
import {Percentage, Duration} from "./common_types.js"
import {Player, PlayerEventListener} from "./player.js"
import {Volume} from "./volume.js"
import {HtmlPlayer} from "./html_player.js"
import {match, P} from "ts-pattern"
import {TimeUpdate} from "./events.js"

/**
 * Gist of the algorithm: When loading with fade-in, we make the inactive player active and fade
 * it in, while fading out (previous) the active player.
 */
export class FadePlayer extends Player {
  private activePlayer: Player
  private inactivePlayer: Player
  private readonly players: [Player, Player]
  private _fadeOnNextLoad: boolean = false

  private targetVolume: Volume
  private faders: Fader[] = []
  private listeners: Set<PlayerEventListener> = new Set()

  private constructor(active: Player, inactive: Player, targetVolume: Volume) {
    super()
    this.activePlayer = active
    this.inactivePlayer = inactive
    this.players = [active, inactive]
    this.targetVolume = targetVolume
  }
  static create(): FadePlayer {
    const active = HtmlPlayer.create()
    const inactive = HtmlPlayer.create()
    return new FadePlayer(active, inactive, active.getVolume())
  }
  unlisten(callback: PlayerEventListener): void {this.listeners.delete(callback)}
  listen(callback: PlayerEventListener): void {this.listeners.add(callback)}
  skipTo(duration: Duration): void {
    this.stopFaders()
    this.activePlayer.skipTo(duration)
  }
  getVolume(): Volume {return this.targetVolume}
  setVolume(v: Volume): void {
    this.targetVolume = v
    if (this.isFading())
      for (const fader of this.faders)
        fader.setVolume(v)
    else
      this.activePlayer.setVolume(v)
  }

  private isFading(): boolean {return this.faders.some(f => f.isWorking)}
  currentTime(): Duration {return this.activePlayer.currentTime()}
  duration(): Duration {return this.activePlayer.duration()}
  percentageOfSongPlayed(): Percentage {return this.activePlayer.percentageOfSongPlayed()}
  isPaused(): boolean {return this.activePlayer.isPaused()}
  pause(): void {
    this.activePlayer.pause()
    this.inactivePlayer.pause()
  }
  stop(): void {
    for (const player of this.players)
      player.stop()
    this.stopFaders()
  }
  async playCurrentSong(): Promise<void> {
    this.activePlayer.playCurrentSong()
    if (this.isFading())
      this.inactivePlayer.playCurrentSong()
  }
  load(song: Song): void {
    if (this._fadeOnNextLoad) {
      this._fadeOnNextLoad = false
      this.loadFade(song)
    } else {
      this.stop()
      this.activePlayer.load(song)
    }
  }

  fadeOnNextLoad() {
    this._fadeOnNextLoad = true
  }

  private loadFade(song: Song): void {
    // * Swap the active and inactive players.
    [this.inactivePlayer, this.activePlayer] = [this.activePlayer, this.inactivePlayer]
    this.stopFaders()
    for (const listener of this.listeners) {
      this.inactivePlayer.unlisten(listener)
      this.activePlayer.listen(listener)
    }
    this.activePlayer.load(song)
    this.faders = [
      this.createFader(this.activePlayer, "fade-in"),
      this.createFader(this.inactivePlayer, "fade-out"),
    ]
  }

  private createFader(player: Player, mode: Mode): Fader {
    const result = Fader.create(player, this.inactivePlayer.duration(), this.targetVolume, mode)
    this.faders.push(result)
    return result
  }

  private stopFaders(): void {
    for (const fader of this.faders)
      fader.finish()
    this.faders = []
  }
}

type Mode = "fade-in" | "fade-out"
class FadeIn {
  targetVolume: Volume
  readonly targetDuration: Duration
  constructor(targetVolume: Volume, targetDuration: Duration) {
    this.targetVolume = targetVolume
    this.targetDuration = targetDuration
  }
}
class FadeOut {
  startingVolume: Volume
  readonly remainingDuration: Duration
  constructor(startingVolume: Volume, remainingDuration: Duration) {
    this.startingVolume = startingVolume
    this.remainingDuration = remainingDuration
  }
}

class Fader {
  private readonly aux: FadeIn | FadeOut
  private player: Player
  private listener!: PlayerEventListener
  isWorking: boolean = true

  static create(
    player: Player,
    duration: Duration,
    targetVolume: Volume,
    mode: Mode,
  ): Fader {
    const aux = match(mode)
      .with("fade-in", () => new FadeIn(targetVolume, duration))
      .with("fade-out", () => new FadeOut(targetVolume, player.currentTime()))
      .exhaustive()
    const result = new Fader(player, aux)
    const listener: PlayerEventListener = e => {
      if (e instanceof TimeUpdate)
        result.timeUpdate(e)
    }
    result.listener = listener
    return result
  }

  private constructor(
    player: Player,
    aux: FadeIn | FadeOut
  ) {
    this.player = player
    this.aux = aux
  }

  setVolume(v: Volume) {
    match(this.aux)
      .with(P.instanceOf(FadeIn), fadeIn => fadeIn.targetVolume = v)
      .with(P.instanceOf(FadeOut), fadeOut => fadeOut.startingVolume = v)
      .exhaustive()
  }

  finish() {
    this.player.unlisten(this.listener)
    this.player.setVolume(match(this.aux)
      .with(P.instanceOf(FadeIn), fadeIn => fadeIn.targetVolume)
      .with(P.instanceOf(FadeOut), _ => new Volume(Percentage.ZERO))
      .exhaustive()
    )
    this.isWorking = false
  }

  private timeUpdate(tu: TimeUpdate) {
    const percentage =
      match(this.aux)
        .with(P.instanceOf(FadeIn), fadeIn => tu.currentDuration.percentage(fadeIn.targetDuration))
        .with(P.instanceOf(FadeOut), fadeOut => {
          assert(
            tu.remainingDuration().isGreaterThanOrEqual(fadeOut.remainingDuration),
            "Remaining duration somehow got larger; skipTo should have stopped the fader.",
          )
          return tu.remainingDuration().percentage(fadeOut.remainingDuration)
        })
        .exhaustive()
    const isFinished = percentage.zeroToOne() >= 0.99
    if (isFinished)
      this.finish()
  }
}
