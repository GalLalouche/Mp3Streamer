/**
 * GUI updates go *in* here, but not *from* here. In other words, updating the GUI (for example,
 * the current playing track, duration, toggling play/pause) should be routed to functions defined
 * here.
 *
 * See {@link event_setup.ts} for the other direction.
 */
import {Duration, Percentage} from "./common_types.js"
import {Song} from "./media.js"
import {Volume} from "./volume.js"
import * as Dom from "./dom.js"


/**
 * The backend (player, playlist) use these functions to update the GUI. It does not control the
 * backend, i.e., no buttons or whatever.
 */
export function setIsPlaying(): void {
  Dom.play.hide()
  Dom.pause.show()
}

export function setIsStopped(): void {
  Dom.pause.hide()
  Dom.play.show()
}

export function setCurrentSong(song: Song): void {
  Dom.duration.html(song.duration.timeFormat())
  const songInfo = `${song.artistName} - ${song.title}`
  Dom.currentlyPlaying.html(songInfo)
  document.title = songInfo
}

export function updatePosition(input: {current: Duration, total: Duration}): void {
  // FIXME take this from the actual seekable value
  //  seekPercent = (this.status.duration > 0) ? 100 * this.html.seekable.end(t.seekable.length - 1) / this.status.duration : 100
  Dom.seekBar.setWidth(Percentage.MAX)
  const totalSeconds = input.total.toSeconds()
  if (totalSeconds > 0)
    Dom.playBar.setWidth(Percentage.fromMax1(input.current.toSeconds() / totalSeconds))
  Dom.currentTime.html(input.current.timeFormat())
}

export function updateVolume(v: Volume): void {
  Dom.volumeBarValue.setWidth(v.percentage())
  if (v.isMuted()) {
    Dom.mute.hide()
    Dom.unmute.show()
  } else {
    Dom.mute.show()
    Dom.unmute.hide()
  }
}
