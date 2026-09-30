import {PubSub, topic} from "./pubsub.js";

import {Percentage, Volume} from "./types.js";

export const GuiEvents: PubSub = new PubSub()

export type PlayerControls =
  "play"
  | "pause"
  | "stop"
  | Volume
export const PlayerControlsTopic = topic<PlayerControls>("player_controls")

export interface PlaylistClicks {
  readonly type: "select" | "x" | "up" | "down"
  readonly index: number
}

export type PlaylistEvent = PlaylistClicks | "next" | "prev"

export const PlaylistEventTopic = topic<PlaylistEvent>("playlist_event")

$(function () {
  function playerControlsAux(str: PlayerControls): void {
    $('.jp-' + str).on('click', () => GuiEvents.publish(PlayerControlsTopic, str))
  }

  playerControlsAux("play")
  playerControlsAux("stop")
  playerControlsAux("pause")

  const volumeBar = $('.jp-volume-bar')
  volumeBar.on('click', (e) => {
    const target = volumeBar
    const offset = target.offset()!
    const x = e.pageX - offset.left
    const w = target.width()!
    const v = x / w
    GuiEvents.publish(PlayerControlsTopic, Volume.fromPercentage(Percentage.fromMax1(v)))
  })

  function playlistControlsAux(str: PlaylistEvent): void {
    $('.jp-' + str).on('click', () => GuiEvents.publish(PlaylistEventTopic, str))
  }

  playlistControlsAux("next")
  playlistControlsAux("prev")
})