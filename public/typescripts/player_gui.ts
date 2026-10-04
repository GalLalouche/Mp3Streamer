/**
 * GUI updates go *in* here, but not *from* here. In other words, updating the GUI (for example,
 * the current playing track, duration, toggling play/pause) should be routed to functions defined
 * here.
 *
 * See GuiEvents for the other direction.
 */

// FIXME wthere really shouldn't be too different module, since it's the same DOM being manipulated.

import {Duration} from "./common_types.js";
import {Song} from "./media.js";
import {Volume} from "./types.js";

let currentTime: JQuery<HTMLElement>
let currentlyPlaying: JQuery<HTMLElement>
let duration: JQuery<HTMLElement>
let muteButton: JQuery<HTMLElement>
let pauseButton: JQuery<HTMLElement>
let playBar: JQuery<HTMLElement>
let seekBar: JQuery<HTMLElement>
let playButton: JQuery<HTMLElement>
let poster: JQuery<HTMLElement>
let unmuteButton: JQuery<HTMLElement>
let volumeBar: JQuery<HTMLElement>

$(function () {
  currentTime = $('.jp-current-time')
  currentlyPlaying = $('.jp-currently-playing')
  duration = $('.jp-duration')
  muteButton = $('.jp-mute')
  pauseButton = $('.jp-pause')
  playBar = $('.jp-play-bar')
  playButton = $('.jp-play')
  poster = $('#jp_poster_0')
  seekBar = $('.jp-seek-bar')
  unmuteButton = $('.jp-unmute')
  volumeBar = $('.jp-volume-bar-value')
})

/**
 * The backend (player, playlist) use these functions to update the GUI. It does not control the
 * backend, i.e., no buttons or whatever.
 */
export function setIsPlaying(): void {
  playButton.hide()
  pauseButton.show()
}

export function setIsStopped(): void {
  pauseButton.hide()
  playButton.show()
}

export function setCurrentSong(song: Song): void {
  const songInfo = `${song.artistName} - ${song.title}`;
  duration.html(song.duration.timeFormat())
  currentlyPlaying.html(songInfo)
  document.title = songInfo
}

export function updatePosition(input: { current: Duration, total: Duration }): void {
  // FIXME take this from the actual seekable value
  //  seekPercent = (this.status.duration > 0) ? 100 * this.html.seekable.end(t.seekable.length - 1) / this.status.duration : 100
  const current = input.current
  const total = input.total
  seekBar.css("width", "100%")
  playBar.css("width", `${100 * current.toSeconds() / total.toSeconds()}%`)
  currentTime.html(current.timeFormat())
}

export function updateVolume(v: Volume): void {
  volumeBar.css("width", `${v.percentage().zeroToHundred()}%`)
  if (v.isMuted()) {
    muteButton.hide()
    unmuteButton.show()
  } else {
    muteButton.show()
    unmuteButton.hide()
  }
}