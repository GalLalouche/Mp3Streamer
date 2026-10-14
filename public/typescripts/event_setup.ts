import {Percentage} from "./common_types.js"
import {mute, Events, PlayerControl, PlayerControls, PlaylistEvent, PlaylistEvents, Seek} from "./events.js"
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
  function playerControlsAux(str: PlayerControl): void {
    $('.jp-' + str).on('click', () => Events.publish(PlayerControls, str))
  }

  playerControlsAux("play")
  playerControlsAux("stop")
  playerControlsAux("pause")


  $('.jp-mute').on('click', () => mute())

  // TODO handle unmute by maintaining the original volume
  function barListener(selector: string, f: (p: Percentage) => PlayerControl): void {
    const target = $(selector)
    target.on('click', e => {
      const clickXPosition = e.pageX - target.offset()!.left
      Events.publish(PlayerControls, f(Percentage.fromMax1(clickXPosition / target.width()!)))
    })
  }

  barListener('.jp-volume-bar', p => new Volume(p))
  barListener('.jp-seek-bar', p => new Seek(p))

  /*********************
   * Playlist controls *
   ********************/
  function playlistControlsAux(str: PlaylistEvent): void {
    $('.jp-' + str).on('click', () => Events.publish(PlaylistEvents, str))
  }

  playlistControlsAux("next")
  playlistControlsAux("previous")
})

