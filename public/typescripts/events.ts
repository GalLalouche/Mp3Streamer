/**
 * Playlist and player PubSub topics. For the individual components (external, lyrics, etc.) look to
 * those modules specifically.
 *
 * Exists to decouple the GUI from the player and playlist logic.
 */
import {Duration, Percentage} from "./common_types.js"
import {Deferred} from "./deferred.js"
import {Song} from "./media.js"
import {Player} from "./player.js"
import {Playlist} from "./playlist.js"
import {PubSub, topic} from "./pubsub.js"
import {Volume} from "./volume.js"


export const Events: PubSub = new PubSub()

/**********
* Startup *
**********/
export const Startup = new Deferred<{player: Player, playlist: Playlist}>()

/*********
* Player *
*********/
export class Seek {
  readonly percentage: Percentage
  constructor(percentage: Percentage) {this.percentage = percentage}
}

export type PlayerControl = "play" | "pause" | "toggle_pause" | "stop" | Volume | Seek | Song
export const PlayerControls = topic<PlayerControl>("player_controls")

export class TimeUpdate {
  readonly currentDuration: Duration
  readonly totalDuration: Duration

  constructor(input: {currentDuration: Duration, totalDuration: Duration}) {
    this.currentDuration = input.currentDuration
    this.totalDuration = input.totalDuration
  }

  remainingDuration(): Duration {
    return this.totalDuration.minus(this.currentDuration)
  }
}

export function mute() {
  Events.publish(PlayerControls, new Volume(Percentage.ZERO))
}

export type PlayerEvent = "ENDED" | TimeUpdate | Song
export const PlayerEvents = topic<PlayerEvent>("player_event")

/***********
* Playlist *
************/
export type PlaylistEvent = "next" | "previous"
export const PlaylistEvents = topic<PlaylistEvent>("playlist_event")

interface PlaylistItemContextMenuEvent {
  readonly type: "external" | "score"
  readonly song: Song
}
export const PlaylistContextMenuEvents = topic<PlaylistItemContextMenuEvent>("playlist_context_menu_event")

