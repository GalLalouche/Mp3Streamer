import {Duration, Song, Volume} from "./types.js";
import {SecondaryPlayer} from "./secondary_player.js";

export namespace FadeOut {
  let fadingPlayer: FadingPlayer | undefined

  export function startFadeOut(song: Song): void {
    stop()
    fadingPlayer = FadingPlayer.start(song)
    // const fadeoutDuration =
    //   Duration.fromSeconds(Math.min(FADEOUT_DURATION, song.duration.toSeconds()))
    // const startAt = song.duration.minus(fadeoutDuration)
    //
    // SecondaryPlayer.play(song, startAt)
    // interval = setInterval(fadeAway, 50, startAt, Date.now())
  }

  // function fadeAway(song: Song, fadeoutDuration: Duration, startTimeInMillis: number): void {
  // }

  export function stop() {
    if (fadingPlayer) {
      fadingPlayer.stop()
    }
  }

  export function pause() {
    if (fadingPlayer) {
      fadingPlayer.pause()
    }
  }

  export function resume() {
    if (fadingPlayer) {
      fadingPlayer.resume()
    }
  }

  export function updateVolume(v: Volume): void {
    if (fadingPlayer) {
      fadingPlayer.updateVolume(v)
    }
  }

  class FadingPlayer {
    private static readonly FADEOUT_DURATION = Duration.fromSeconds(5)
    private static readonly INTERVAL = Duration.fromMillis(50)
    private readonly song: Song
    private intervalID!: number
    private readonly fadeoutDuration: Duration;
    private readonly startDuration: Duration;
    private readonly startTimeInMillis: number = Date.now()

    constructor(song: Song) {
      this.song = song
      this.fadeoutDuration = FadingPlayer.FADEOUT_DURATION.min(song.duration)
      this.startDuration = song.duration.minus(this.fadeoutDuration)
    }

    static start(song: Song): FadingPlayer {
      const result = new FadingPlayer(song)
      SecondaryPlayer.play(song, result.startDuration)
      result.intervalID = setInterval(() => result.tick(), FadingPlayer.INTERVAL.toMillis())
      return result
    }

    private tick(): void {
      const now = Date.now()
      const elapsed = Duration.fromMillis(now - this.startTimeInMillis)
      if (elapsed.isGreaterThanOrEqual(this.fadeoutDuration)) {
        this.stop()
        return
      }

      SecondaryPlayer.updateVolumeRatio(elapsed.toMillis() / this.fadeoutDuration.toMillis())
    }

    resume(): void {
      notImplemented()
    }

    stop(): void {
      SecondaryPlayer.reset()
      clearInterval(this.intervalID)
      fadingPlayer = undefined
    }

    pause(): void {
      SecondaryPlayer.pause()
      clearInterval(this.intervalID)
    }

    updateVolume(v: Volume): void {
      notImplemented()
    }
  }
}
