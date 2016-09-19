$(function () {
  const defaultGain = -10.0
  var volumeBaseline = 40.0 // The volume that was preset by the user. Start at 40.0, so it could increase 2.5 fold.
  var currentGain = defaultGain

  const calculateVolumeCoefficientFromGain = () => Math.pow(2, currentGain / 10.0)
  function updateVolume() {
    // +10 dB is twice as loud. Or something.
    gplayer.setVolume(volumeBaseline * calculateVolumeCoefficientFromGain())
  }

  Volume.setManualVolume = function (v) {
    v = v < 1 && v > 0 ? v * 100 : v // if v is between 0 and 1, convert to be between 0 and 100
    volumeBaseline = v / calculateVolumeCoefficientFromGain()
    updateVolume()
  }
  Volume.setPeak = function (song) {
    currentGain = song.trackGain || defaultGain
    updateVolume()
  }
})
Volume = {}
