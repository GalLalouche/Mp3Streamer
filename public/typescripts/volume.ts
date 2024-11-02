import {gplayer, Song} from "./types.js"

const DEFAULT_GAIN = -10.0

// This isn't a namespace since it actually makes sense as an object.
export class Volume {
  // The volume that was preset by the user. Start at 20.0, so it could increase 5-fold.
  private static volumeBaseline: number = 20.0
  private static currentGain: number = DEFAULT_GAIN
  private static calculateVolumeCoefficientFromGain(): number {
    return Math.pow(2, this.currentGain / 10.0)
  }
  static updateVolume(): void {
    // +10 dB is twice as loud. Or something.
    gplayer.setVolume(this.volumeBaseline * this.calculateVolumeCoefficientFromGain())
  }
  static setManualVolume(v: number): void {
    // if v is between 0 and 1, convert to be between 0 and 100
    this.volumeBaseline =
      (v < 1 && v > 0 ? v * 100 : v) / this.calculateVolumeCoefficientFromGain()
    this.updateVolume()
  }
  static setPeak(song: Song) {
    this.currentGain = song.trackGain || DEFAULT_GAIN
    this.updateVolume()
  }
  // Volume.setManualVolume(Volume.getVolumeBaseline) should be a no-op.
  static getVolumeBaseline(): number {
    return this.volumeBaseline * this.calculateVolumeCoefficientFromGain()
  }
}

$exposeGlobally!(Volume)
