$(function() {
  const DEFAULT_GAIN = -10.0
  let volumeBaseline = 20.0 // The volume that was preset by the user. Start at 20.0, so it could increase 5 fold.
  let currentGain = DEFAULT_GAIN

  const calculateVolumeCoefficientFromGain = () => Math.pow(2, currentGain / 10.0)

  function updateVolume() {
    // +10 dB is twice as loud. Or something.
    gplayer.setVolume(volumeBaseline * calculateVolumeCoefficientFromGain())
  }

  Volume.setManualVolume = function(v) {
     // if v is between 0 and 1, convert to be between 0 and 100
    volumeBaseline = (v < 1 && v > 0 ? v * 100 : v) / calculateVolumeCoefficientFromGain()
    updateVolume()
  }
  Volume.setPeak = function(song) {
    currentGain = song.trackGain || DEFAULT_GAIN
    updateVolume()
  }
  // Volume.setManualVolume(Volume.getVolumeBaseline) should be a no-op.
  Volume.getVolumeBaseline = function() {
    return volumeBaseline * calculateVolumeCoefficientFromGain()
  }
})
Volume = {}
