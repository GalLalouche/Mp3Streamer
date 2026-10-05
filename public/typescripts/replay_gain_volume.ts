import {Percentage} from "./common_types.js"
import {Song} from "./media.js"
import {Volume} from "./types.js"

export class ReplayGainAwareVolume {
  private static readonly DEFAULT_GAIN = -10.0

  // The volume that was preset by the user. Start at 20.0, so it could increase 5-fold.
  private volumeBaseline: number = 20.0 // In 0 to 100 units, but can actually pass 100 before scaling.
  private currentGain: number = ReplayGainAwareVolume.DEFAULT_GAIN

  setManualVolume(v: Volume): Volume {
    this.volumeBaseline = v.percentage().zeroToHundred() / this.calculateVolumeCoefficientFromGain()
    return this.replayGainAdjustedVolume()
  }
  setPeak(song: Song): Volume {
    this.currentGain = song.trackGain
    return this.replayGainAdjustedVolume()
  }
  replayGainAdjustedVolume(): Volume {
    const p = this.volumeBaseline * this.calculateVolumeCoefficientFromGain()
    return new Volume(Percentage.fromMax100(Math.min(p, 100)))
  }

  private calculateVolumeCoefficientFromGain(): number {
    return Math.pow(2, this.currentGain / 10.0)
  }
}
