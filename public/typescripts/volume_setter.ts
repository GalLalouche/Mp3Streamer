import {gplayer} from "./player_singleton.js";
import {Percentage, Song, Volume} from "./types.js"

const DEFAULT_GAIN = -10.0

// The volume that was preset by the user. Start at 20.0, so it could increase 5-fold.
let volumeBaseline: number = 20.0 // In 0 to 100 units, but can actually pass 100 before scaling.
let currentGain: number = DEFAULT_GAIN

function calculateVolumeCoefficientFromGain(): number {
  return Math.pow(2, currentGain / 10.0)
}

function updateVolume(): void {
  // +10 dB is twice as loud. Or something.
  gplayer.setVolume(getVolumeBaseline())
}

export function setManualVolume(v: Volume): void {
  volumeBaseline = v.percentage().zeroToHundred() / calculateVolumeCoefficientFromGain()
  updateVolume()
}

export function setPeak(song: Song) {
  currentGain = song.trackGain || DEFAULT_GAIN
  updateVolume()
}

// VolumeSetter.setManualVolume(VolumeSetter.getVolumeBaseline) should be a no-op.
export function getVolumeBaseline(): Volume {
  const p = volumeBaseline * calculateVolumeCoefficientFromGain();
  return Volume.fromPercentage(Percentage.fromMax100(Math.min(p, 100)))
}
