import {Duration, Percentage} from "./common_types.js"
import {Events, PlayerEvent, PlayerEvents, TimeUpdate} from "./events.js"
import {Song} from "./media.js"
import * as PlayerGUI from "./player_gui.js"
import {Player} from "./player.js"
import {Volume} from "./volume.js"
import {HtmlPlayer} from "./html_player.js"

export function makePlayer(): Player {
  const player = HtmlPlayer.create()
  const volume: ReplayGainAwareVolume = new ReplayGainAwareVolume()

  const result = new class extends Player {
    override clear(): void {player.clear()}
    override currentTime(): Duration {return player.currentTime()}
    override getVolume(): Volume {return volume.replayGainAdjustedVolume()}
    override isPaused(): boolean {return player.isPaused()}
    override load(song: Song): void {
      player.load(song)
      player.setVolume(volume.setPeak(song.trackGain))
    }
    override percentageOfSongPlayed(): Percentage {return player.percentageOfSongPlayed()}
    override duration(): Duration {return player.duration()}
    override playCurrentSong(): Promise<void> {
      PlayerGUI.setIsPlaying()
      return player.playCurrentSong()
    }
    override pause(): void {
      player.pause()
      PlayerGUI.setIsStopped()
    }
    override setVolume(v: Volume): void {
      player.setVolume(volume.setManualVolume(v))
      PlayerGUI.updateVolume(v)
    }
    override skipTo(duration: Duration): void {return player.skipTo(duration)}
    override stop(): void {
      player.stop()
      PlayerGUI.setIsStopped()
    }
    override listen(callback: (pe: PlayerEvent) => void): void {player.listen(callback)}
    override unlisten(callback: (pe: PlayerEvent) => void): void {player.unlisten(callback)}
  }
  result.setVolume(volume.replayGainAdjustedVolume())

  result.listen((pe: PlayerEvent) => {
    if (pe instanceof TimeUpdate)
      PlayerGUI.updatePosition({
        current: result.currentTime(),
        total: result.duration(),
      })
    if (pe instanceof Song)
      PlayerGUI.setCurrentSong(pe)
    Events.publish(PlayerEvents, pe)
  })
  return result
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

