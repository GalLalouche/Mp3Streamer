import {gplayer} from "./player_singleton.js";
import {Percentage, Song, Volume} from "./types.js"

const DEFAULT_GAIN = -10.0

// This isn't a namespace since it actually makes sense as an object.
export class VolumeSetter {
  // The volume that was preset by the user. Start at 20.0, so it could increase 5-fold.
  private static volumeBaseline: number = 20.0 // In 0 to 100 units, but can actually pass 100 before scaling.
  private static currentGain: number = DEFAULT_GAIN
  private static calculateVolumeCoefficientFromGain(): number {
    return Math.pow(2, this.currentGain / 10.0)
  }
  private static updateVolume(): void {
    // +10 dB is twice as loud. Or something.
    gplayer.setVolume(this.getVolumeBaseline())
  }
  static setManualVolume(v: Volume): void {
    this.volumeBaseline = v.percentage().zeroToHundred() / this.calculateVolumeCoefficientFromGain()
    this.updateVolume()
  }
  static setPeak(song: Song) {
    this.currentGain = song.trackGain || DEFAULT_GAIN
    this.updateVolume()
  }
  // VolumeSetter.setManualVolume(VolumeSetter.getVolumeBaseline) should be a no-op.
  static getVolumeBaseline(): Volume {
    const p = this.volumeBaseline * this.calculateVolumeCoefficientFromGain();
    return Volume.fromPercentage(Percentage.fromMax100(Math.min(p, 100)))
  }
}

$exposeGlobally!(VolumeSetter)
