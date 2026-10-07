/**
 * Playlist and player events. For the individual components like external, lyrics, etc. look to
 * those modules specifically.
 *
 * Exists to decouple the GUI from the player and playlist logic.
 */
import {Percentage} from "./common_types.js"
import {PubSub, topic} from "./pubsub.js"
import {Volume} from "./volume.js"


export const GuiEvents: PubSub = new PubSub()

export class Seek {
  readonly percentage: Percentage

  constructor(percentage: Percentage) {
    this.percentage = percentage
  }
}

export type PlayerControls = "play" | "pause" | "stop" | Volume | Seek
export const PlayerControlsTopic = topic<PlayerControls>("player_controls")

export interface PlaylistClicks {
  readonly type: "select" | "x" | "up" | "down"
  readonly index: number
}

export type PlaylistEvent = PlaylistClicks | "next" | "previous"

export const PlaylistEventTopic = topic<PlaylistEvent>("playlist_event")

$(function () {
  /*******************
   * Player controls *
   ******************/
  function playerControlsAux(str: PlayerControls): void {
    $('.jp-' + str).on('click', () => GuiEvents.publish(PlayerControlsTopic, str))
  }

  playerControlsAux("play")
  playerControlsAux("stop")
  playerControlsAux("pause")


  $('.jp-mute').on('click', () => GuiEvents.publish(PlayerControlsTopic, new Volume(Percentage.ZERO)))

  // TODO handle unmute by maintaining the original volume
  function barListener(selector: string, f: (p: Percentage) => PlayerControls): void {
    const target = $(selector)
    target.on('click', e => {
      const clickXPosition = e.pageX - target.offset()!.left
      GuiEvents.publish(PlayerControlsTopic, f(Percentage.fromMax1(clickXPosition / target.width()!)))
    })
  }

  barListener('.jp-volume-bar', p => new Volume(p))
  barListener('.jp-seek-bar', p => new Seek(p))

  /*********************
   * Playlist controls *
   ********************/
  function playlistControlsAux(str: PlaylistEvent): void {
    $('.jp-' + str).on('click', () => GuiEvents.publish(PlaylistEventTopic, str))
  }

  playlistControlsAux("next")
  playlistControlsAux("previous")
  $(".jp-playlist").on("click", "> ul > li", function (e) {
    const listItem = $(this)
    const totalSongs = $(this).parent().children().length
    // The list presentation reversed, so song at index 0 is actually the last song, not the first.
    // FIXME remove the method on the gplaylist
    const clickedIndex: number = totalSongs - 1 - listItem.index()
    const target = $(e.target)

    function getRemoveType(target: JQuery<HTMLElement>): "x" | "up" | "down" {
      if (target.hasClass("jp-playlist-item-remove-this")) {
        return "x"
      } else if (target.hasClass("jp-playlist-item-remove-up")) {
        return "up"
      } else if (target.hasClass("jp-playlist-item-remove-down")) {
        return "down"
      } else {
        throw new Error("Unknown remove type: " + target.attr("class"))
      }
    }

    function publish(type: "x" | "up" | "down" | "select") {
      GuiEvents.publish(PlaylistEventTopic, {type: type, index: clickedIndex})
    }

    if (target.hasClass("jp-playlist-item-remove")) {
      publish(getRemoveType(target))
    } else if (e.target.localName === "span" || e.target.localName === "img") {
      publish("select")
    }
  })
})
