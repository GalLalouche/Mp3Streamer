declare const gplayer: Player
const DEFAULT_GAIN = -10.0

class Volume {
    // The volume that was preset by the user. Start at 20.0, so it could increase 5-fold.
    private volumeBaseline: number = 20.0
    private currentGain: number = DEFAULT_GAIN
    private calculateVolumeCoefficientFromGain(): number {
        return Math.pow(2, this.currentGain / 10.0)
    }
    updateVolume(): void {
        // +10 dB is twice as loud. Or something.
        gplayer.setVolume(this.volumeBaseline * this.calculateVolumeCoefficientFromGain())
    }
    setManualVolume(v: number): void {
        // if v is between 0 and 1, convert to be between 0 and 100
        this.volumeBaseline =
            (v < 1 && v > 0 ? v * 100 : v) / this.calculateVolumeCoefficientFromGain()
        this.updateVolume()
    }
    setPeak(song: Song) {
        this.currentGain = song.trackGain || DEFAULT_GAIN
        this.updateVolume()
    }
    // Volume.setManualVolume(Volume.getVolumeBaseline) should be a no-op.
    getVolumeBaseline(): number {
        return this.volumeBaseline * this.calculateVolumeCoefficientFromGain()
    }
}

(window as any).Volume = new Volume()
