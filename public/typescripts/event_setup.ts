import {Percentage} from "./common_types.js"
import * as Dom from "./dom.js"
import {Events, mute, PlayerControl, PlayerControlButton, PlayerControls, PlaylistEvent, PlaylistEvents, Seek} from "./events.js"
import {Volume} from "./volume.js"

export const REMOVE_ITEM = "jp-playlist-item-remove"
export const REMOVE_THIS = "jp-playlist-item-remove-this"
export const REMOVE_UP = "jp-playlist-item-remove-up"
export const REMOVE_DOWN = "jp-playlist-item-remove-down"

export const ITEM_CLASS = "jp-playlist-item"


$(function () {
  /*******************
   * Player controls *
   ******************/
  function playerControlsAux(b: PlayerControlButton): void {
    const element = Dom.playerControlButton(b)
    element.on('click', () => Events.publish(PlayerControls, b))
  }

  playerControlsAux("play")
  playerControlsAux("stop")
  playerControlsAux("pause")


  $(Dom.mute).on('click', () => mute())

  // TODO handle unmute by maintaining the original volume
  function barListener(target: JQuery<HTMLElement>, f: (p: Percentage) => PlayerControl): void {
    target.on('click', e => {
      const clickXPosition = e.pageX - target.offset()!.left
      Events.publish(PlayerControls, f(Percentage.fromMax1(clickXPosition / target.width()!)))
    })
  }

  barListener(Dom.volumeBar, p => new Volume(p))
  barListener(Dom.seekBar, p => new Seek(p))

  /*********************
   * Playlist controls *
   ********************/
  function playlistControlsAux(str: PlaylistEvent): void {
    Dom.playlistControlButton(str).on('click', () => Events.publish(PlaylistEvents, str))
  }

  playlistControlsAux("next")
  playlistControlsAux("previous")
})

