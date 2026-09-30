import {Duration, Percentage, Player, PlayerEvent, Song, Volume} from "./types.js";
import {PlayerImpl} from "./player.js";
import {PlayerGUI} from "./player_gui.js";
import {VolumeSetter} from "./volume_setter.js";
import {GuiEvents, PlayerControls, PlayerControlsTopic} from "./gui_events.js";

interface JPlayerElement {
  jPlayer(str: String, value?: any): void
  data(): any
}

export type EventForPlaylist = "READY" | "ENDED"


export const EventsForPlaylist = "#playlist_events"
export const ElementEventForPlaylist = "event_for_playlist"
let playerEvents!: HTMLElement
declare global {
  export interface HTMLElementEventMap {
    "event_for_playlist": CustomEvent<EventForPlaylist>
  }
}

class SingletonPlayer extends Player {
  private readonly player: Player

  private constructor(player: Player) {
    super()
    this.player = player
  }
  static from(player: Player): SingletonPlayer {
    const result = new SingletonPlayer(player)
    result.listen((pe: PlayerEvent) => {
      if (pe == "ENDED")
        playerEvents.dispatchEvent(
          new CustomEvent<EventForPlaylist>(ElementEventForPlaylist, {detail: "ENDED"}))
      else {
        PlayerGUI.updatePosition({
          current: result.currentTime(),
          total: result.duration()
        })
        PlayerGUI.setIsPlaying()
      }
      //   this.html.onpause = () => PlayerGUI.setIsStopped()
      //   this.html.onplay = () => PlayerGUI.setIsPlaying()
      //   this.html.onvolumechange = () => PlayerGUI.updateVolume(this.getVolume())
    })
    // TODO these listens should be made elsewhere
    GuiEvents.listen(PlayerControlsTopic, (control: PlayerControls) => {
      if (control == "play")
        result.playCurrentSong()
      else if (control == "stop")
        result.stop()
      else if (control == "pause")
        result.pause()
      else if (control instanceof Volume)
        VolumeSetter.setManualVolume(control)
    })
    return result
  }
  override clear(): void {
    this.player.clear();
  }
  override currentTime(): Duration {
    return this.player.currentTime();
  }
  override getVolume(): Volume {
    return this.player.getVolume();
  }
  override isPaused(): boolean {
    return this.player.isPaused();
  }
  override load(song: Song): void {
    this.player.load(song);
  }
  override percentageOfSongPlayed(): Percentage {
    return this.player.percentageOfSongPlayed();
  }
  override duration(): Duration {
    return this.player.duration();
  }
  override playCurrentSong(): void {
    this.player.playCurrentSong();
  }
  override pause(): void {
    this.player.pause();
    PlayerGUI.setIsStopped()
  }
  override setVolume(v: Volume): void {
    this.player.setVolume(v)
    PlayerGUI.updateVolume(v)
  }
  override skipTo(duration: Duration): void {
    return this.player.skipTo(duration);
  }
  override stop(): void {
    this.player.stop();
    PlayerGUI.setIsStopped()
  }
  override listen(callback: (pe: PlayerEvent) => void): void {
    this.player.listen(callback);
  }
  override unlisten(callback: (pe: PlayerEvent) => void): void {
    this.player.unlisten(callback);
  }
}

// TODO temporary, until this is refactored to use a proper singleton method.
export let gplayer!: Player
// TODO extract?
Object.defineProperty(globalThis, "gplayer", {
  get: (): Player => gplayer,
  set: (value: Player) => { gplayer = value },
  configurable: true,
})

$(function () {
  gplayer = SingletonPlayer.from(PlayerImpl.create())
  gplayer.setVolume(VolumeSetter.getVolumeBaseline())
  playerEvents = document.createElement("div")
  playerEvents.id = EventsForPlaylist
  playerEvents.dispatchEvent(new CustomEvent<EventForPlaylist>(ElementEventForPlaylist, {detail: "READY"}))
})
window.EventsForPlaylist = EventsForPlaylist
declare global {
  var EventsForPlaylist: string
  var ElementEventForPlaylist: string
}
globalThis.EventsForPlaylist = EventsForPlaylist
globalThis.ElementEventForPlaylist = ElementEventForPlaylist
// FIXME why did this stop working?
// $exposeGlobally!(EventsForPlaylist)
// $exposeGlobally!(ElementEventForPlaylist)
