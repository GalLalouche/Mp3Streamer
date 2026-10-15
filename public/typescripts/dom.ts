import {match} from "ts-pattern"
import {PlayerControlButton, PlaylistEvent} from "./events"

export let currentTime: JQuery<HTMLElement>
export let currentlyPlaying: JQuery<HTMLElement>
export let duration: JQuery<HTMLElement>
export let mute: JQuery<HTMLElement>
export let next: JQuery<HTMLElement>
export let pause: JQuery<HTMLElement>
export let play: JQuery<HTMLElement>
export let playBar: JQuery<HTMLElement>
export let poster: JQuery<HTMLElement>
export let previous: JQuery<HTMLElement>
export let seekBar: JQuery<HTMLElement>
export let stop: JQuery<HTMLElement>
export let unmute: JQuery<HTMLElement>
export let volumeBar: JQuery<HTMLElement>
export let volumeBarValue: JQuery<HTMLElement>

$(function () {
  currentTime = $('.jp-current-time')
  currentlyPlaying = $('.jp-currently-playing')
  duration = $('.jp-duration')
  mute = $('.jp-mute')
  next = $('.jp-next')
  pause = $('.jp-pause')
  play = $('.jp-play')
  playBar = $('.jp-play-bar')
  poster = $('#jp_poster_0')
  previous = $('.jp-previous')
  seekBar = $('.jp-seek-bar')
  stop = $('.jp-stop')
  unmute = $('.jp-unmute')
  volumeBar = $('.jp-volume-bar')
  volumeBarValue = $('.jp-volume-bar-value')
})

export function playerControlButton(b: PlayerControlButton): JQuery<HTMLElement> {
  return match(b)
    .with("play", () => play)
    .with("stop", () => stop)
    .with("pause", () => pause)
    .exhaustive()
}

export function playlistControlButton(e: PlaylistEvent): JQuery<HTMLElement> {
  return match(e)
    .with("next", () => next)
    .with("previous", () => previous)
    .exhaustive()
}
