$(function () {
  var volumeBaseline = 80.0 // the volume that was preset by the user
  const defaultGain = -10.0 // if there is no replay gain, this is assumed to be the gain
  var currentGain = defaultGain
  function updateVolume() {
    gplayer.setVolume(volumeBaseline + currentGain)
  }

  Volume.setManualVolume = function (v) {
    v = v < 1 && v > 0 ? v * 100 : v // if v is between 0 and 1, convert to be between 0 and 100
    volumeBaseline = v - currentGain // Remove the currentGain from the volume, to ensure continued relative volume
    updateVolume()
  }
  Volume.setPeak = function (song) {
    currentGain = song.trackGain || defaultGain
    updateVolume()
  }
})
Volume = {}
