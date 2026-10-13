/**
 * Playlist and player PubSub topics. For the individual components (external, lyrics, etc.) look to
 * those modules specifically.
 *
 * Exists to decouple the GUI from the player and playlist logic.
 */
import {Duration, Percentage} from "./common_types.js"
import {Deferred} from "./deferred.js"
import {Song} from "./media.js"
import {PubSub, topic} from "./pubsub.js"
import {Volume} from "./volume.js"


export const Events: PubSub = new PubSub()

/**********
* Startup *
**********/
export const Startup = new Deferred<void>()

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


export class TimeUpdate {
  readonly currentDuration: Duration
  readonly totalDuration: Duration

  constructor(input: { currentDuration: Duration, totalDuration: Duration }) {
    this.currentDuration = input.currentDuration
    this.totalDuration = input.totalDuration
  }

  remainingDuration(): Duration {
    return this.totalDuration.minus(this.currentDuration)
  }
}

export type PlayerEvent = "ENDED" | TimeUpdate | Song
export type PlayerEventListener = (pe: PlayerEvent) => void
export const PlayerEvents = topic<PlayerEvent>("player_event")

/***********
* Playlist *
************/

export type PlaylistEvent = "next" | "previous" | Song
export const PlaylistEventTopic = topic<PlaylistEvent>("playlist_event")

interface PlaylistItemContextMenuEvent {
  readonly type: "external" | "score"
  readonly song: Song
}
export const PlaylistContextMenuEventTopic = topic<PlaylistItemContextMenuEvent>("playlist_context_menu_event")

