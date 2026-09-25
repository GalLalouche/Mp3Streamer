import {gplayer, Percentage, Song, Volume} from "./types.js"

const DEFAULT_GAIN = -10.0

// This isn't a namespace since it actually makes sense as an object.
export class VolumeSetter {
  // The volume that was preset by the user. Start at 20.0, so it could increase 5-fold.
  private static volumeBaseline: Volume = Volume.fromPercentage(Percentage.fromMax100(20.0))
  private static currentGain: number = DEFAULT_GAIN
  // TODO use a proper type instead of number
  private static calculateVolumeCoefficientFromGain(): number {
    return Math.pow(2, this.currentGain / 10.0)
  }
  private static updateVolume(): void {
    // +10 dB is twice as loud. Or something.
    gplayer.setVolume(this.volumeBaseline.times(this.calculateVolumeCoefficientFromGain()))
  }
  static setManualVolume(v: Volume): void {
    // if v is between 0 and 1, convert to be between 0 and 100
    this.volumeBaseline = v.times(1 / this.calculateVolumeCoefficientFromGain())
    this.updateVolume()
  }
  static setPeak(song: Song) {
    this.currentGain = song.trackGain || DEFAULT_GAIN
    this.updateVolume()
  }
  // VolumeSetter.setManualVolume(VolumeSetter.getVolumeBaseline) should be a no-op.
  static getVolumeBaseline(): Volume {
    return this.volumeBaseline.times(this.calculateVolumeCoefficientFromGain())
  }
}

$exposeGlobally!(VolumeSetter)
