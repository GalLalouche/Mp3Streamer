/**
 * Playlist and player events. For the individual components like external, lyrics, etc. look to
 * those modules specifically.
 *
 * Exists to decouple the GUI from the player and playlist logic.
 */
import {Percentage} from "./common_types.js"
import {Song} from "./media.js"
import {Player} from "./player.js"
import {Playlist} from "./playlist.js"
import {PubSub, topic} from "./pubsub.js"
import {Volume} from "./volume.js"


export const GuiEvents: PubSub = new PubSub()

// FIXME this isn't a GUI event... perhaps this module should be renamed?
interface PlayerSetup {
  player: Player
  playlist: Playlist
}
export const PlayerSetup = topic<PlayerSetup>("player_setup")

export const REMOVE_ITEM = "jp-playlist-item-remove"
export const REMOVE_THIS = "jp-playlist-item-remove-this"
export const REMOVE_UP = "jp-playlist-item-remove-up"
export const REMOVE_DOWN = "jp-playlist-item-remove-down"

export const ITEM_CLASS = "jp-playlist-item"

export type RemoveType = "x" | "up" | "down"
export type PlaylistClickType = "select" | RemoveType

export class Seek {
  readonly percentage: Percentage

  constructor(percentage: Percentage) {
    this.percentage = percentage
  }
}

export type PlayerControls = "play" | "pause" | "stop" | Volume | Seek
export const PlayerControlsTopic = topic<PlayerControls>("player_controls")

export interface PlaylistClicks {
  readonly type: PlaylistClickType
  readonly index: number
}
export type PlaylistEvent = PlaylistClicks | "next" | "previous"
export const PlaylistEventTopic = topic<PlaylistEvent>("playlist_event")

interface PlaylistContextMenuEvent {
  readonly type: "external" | "score"
  readonly song: Song
}
export const PlaylistContextMenuEventTopic = topic<PlaylistContextMenuEvent>("playlist_context_menu_event")


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

    function getRemoveType(target: JQuery<HTMLElement>): RemoveType {
      if (target.hasClass(REMOVE_THIS)) {
        return "x"
      } else if (target.hasClass(REMOVE_UP)) {
        return "up"
      } else if (target.hasClass(REMOVE_DOWN)) {
        return "down"
      } else {
        throw new Error("Unknown remove type: " + target.attr("class"))
      }
    }

    function publish(type: PlaylistClickType): void {
      GuiEvents.publish(PlaylistEventTopic, {type: type, index: clickedIndex})
    }

    if (target.hasClass(REMOVE_ITEM)) {
      publish(getRemoveType(target))
    } else if (e.target.localName === "span" || e.target.localName === "img") {
      publish("select")
    }
  })

  /****************
   * Context menu *
   ***************/
  $("body").append(String.raw`
    <ul id="contextMenu" class="ui-menu" style="display:none;">
        <li><div><span class="menu-icon fa fa-arrows-v"/></span> Score</div></li>
        <li><div><span class="menu-icon fa fa-refresh"></span> Refresh</div></li>
        <style>
        .ui-menu {
            width: 150px
            background-color: white
            border: 1px solid #ccc
            box-shadow: 2px 2px 5px rgba(0,0,0,0.2)
        }
        .menu-icon {
            margin-right: 5px
            width: 15px
            text-align: center
        }
        </style>
    </ul>
  `)
  const playlistElement = $(".jp-playlist")
  const playlistItem = "> ul > li"
  const contextMenu = $("#contextMenu").menu()
  playlistElement.on("contextmenu", playlistItem, function (e) {
    e.preventDefault() // Prevent the default context menu

    contextMenu.css({
      display: 'block',
      top: e.pageY + 5,
      left: e.pageX + 5,
      position: 'absolute',
    })

    const song = $(this).closest('.' + ITEM_CLASS).data("song") as Song
    contextMenu.one("click", "li", async function (e) {
      switch (e.target.textContent.trim()) {
        case "Score":
          GuiEvents.publish(PlaylistContextMenuEventTopic, {type: "score", song: song})
        case "Refresh":
          GuiEvents.publish(PlaylistContextMenuEventTopic, {type: "external", song: song})
        default:
          throw new AssertionError("Unexpected selection: " + e.target.textContent)
      }
    })

    $(document).one("click", () => $("#contextMenu").hide())
  })
})

