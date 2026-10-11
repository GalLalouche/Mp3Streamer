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

interface PlayerSetup {
  player: Player
  playlist: Playlist
}
export const PlayerSetup = topic<PlayerSetup>("player_setup")

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

