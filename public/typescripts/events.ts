/**
 * Playlist and player PubSub topics. For the individual components (external, lyrics, etc.) look to
 * those modules specifically.
 *
 * Exists to decouple the GUI from the player and playlist logic.
 */
import {Percentage} from "./common_types.js"
import {Deferred} from "./deferred.js"
import {Song} from "./media.js"
import {Player} from "./player.js"
import {Playlist} from "./playlist.js"
import {PubSub, topic} from "./pubsub.js"
import {Volume} from "./volume.js"


export const GuiEvents: PubSub = new PubSub()

/**********
* Startup *
**********/
export const Startup = new Deferred<{player: Player, playlist: Playlist}>()

/*********
* Player *
*********/
export class Seek {
  readonly percentage: Percentage

  constructor(percentage: Percentage) {
    this.percentage = percentage
  }
}

export type PlayerControls = "play" | "pause" | "stop" | Volume | Seek
export const PlayerControlsTopic = topic<PlayerControls>("player_controls")

/***********
* Playlist *
************/

export type PlaylistEvent = "next" | "previous"
export const PlaylistEventTopic = topic<PlaylistEvent>("playlist_event")

// FIXME these should be published on the playlist item only, and registered when the item is created.
export type RemoveType = "x" | "up" | "down"
export type PlaylistItemClickType = "select" | RemoveType
export const PlaylistItemTopic =
  topic<{type: PlaylistItemClickType, element: JQuery<HTMLElement>}>("playlist_event")

interface PlaylistItemContextMenuEvent {
  readonly type: "external" | "score"
  readonly song: Song
}
export const PlaylistContextMenuEventTopic = topic<PlaylistItemContextMenuEvent>("playlist_context_menu_event")

