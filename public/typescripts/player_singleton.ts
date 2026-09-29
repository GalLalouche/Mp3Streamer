import {Duration, Percentage, Player, PlayerEvent, Song, TimeUpdate, Volume} from "./types.js";
import {PlayerImpl} from "./player.js";
import {getSearchParam} from "./initialization.js";
import {PlayerGUI} from "./player_gui.js";

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
    player.listen((pe: PlayerEvent) => {
      if (pe == "ENDED")
        playerEvents.dispatchEvent(new
        CustomEvent<EventForPlaylist>(ElementEventForPlaylist, {detail: "ENDED"}))
      else {
        PlayerGUI.updatePosition({
          current: result.currentTime(),
          total: result.duration()
        })
      }
      //   this.html.onpause = () => PlayerGUI.setIsStopped()
      //   this.html.onplay = () => PlayerGUI.setIsPlaying()
      //   this.html.onvolumechange = () => PlayerGUI.updateVolume(this.getVolume())
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
  }
  setVolume(v: Volume): void {
    this.player.setVolume(v);
  }
  override skipTo(duration: Duration): void {
    return this.player.skipTo(duration);
  }
  override stop(): void {
    this.player.stop();
  }
  listen(callback: (pe: PlayerEvent) => void): void {
    throw new AssertionError("SingletonPlayer.listen() should not be called");
  }
}

// TODO temporary, until this is refactored to use a proper singleton method.
export let gplayer!: Player

function extracted(): Player {
  return getSearchParam("manual_player") != null
    ? new PlayerImpl()
    : new class extends Player {
      private player(): JPlayerElement {return $("#jquery_jplayer_1") as unknown as JPlayerElement}
      override load(song: Song): void {this.player().jPlayer("setMedia", song)}
      private click(what: string): void {$(".jp-" + what).click()}
      override pause(): void {this.click("pause")}
      override stop(): void {this.click("stop")}
      override playCurrentSong(): void {this.player().jPlayer("play")}
      override isPaused(): boolean {return this.player().data().jPlayer.status.paused}
      override percentageOfSongPlayed(): Percentage {
        const jPlayer = this.player().data().jPlayer
        return jPlayer ?
          Percentage.fromMax100(jPlayer.status.currentPercentAbsolute) :
          Percentage.fromMax1(0)
      }
      override duration(): Duration {
        return Duration.fromSeconds(this.player().data().jPlayer.status.duration)
      }
      override currentTime(): Duration {
        return Duration.fromSeconds(this.player().data().jPlayer.status.currentTime)
      }
      private volumeBar() {return $(".jp-volume-bar-value")}
      override getVolume(): Volume {
        return Volume.fromPercentage(Percentage.fromMax100(this.volumeBar().width()!))
      }
      override setVolume(v: Volume): void {
        this.volumeBar().width(`${v}%`)
        this.player().jPlayer("volume", v._volume().zeroToOne())
      }
      override skipTo(duration: Duration): void {this.player().jPlayer("play", duration.toSeconds())}
      override clear(): void {this.player().jPlayer("clearMedia")}
      listen(callback: (pe: PlayerEvent) => void): void {
        const that = this

        function aux(e: string, f: () => void) {
          (that.player() as any as JQuery).bind(($ as any).jPlayer.event[e], f)
        }

        aux("ended", () => {callback("ENDED")})
        aux("timeupdate", () => {
          const jPlayer = that.player().data().jPlayer!
          callback(new TimeUpdate({
            currentDuration: Duration.fromSeconds(jPlayer.status.currentTime),
            totalDuration: that.currentTime()
          }))
        })
      }
    };
}

$(function () {
  gplayer = SingletonPlayer.from(extracted())
  playerEvents = document.createElement("div")
  playerEvents.id = EventsForPlaylist
  playerEvents.dispatchEvent(new CustomEvent<EventForPlaylist>(ElementEventForPlaylist, {detail: "READY"}))
})
$exposeGlobally!(gplayer)
$exposeGlobally!(EventsForPlaylist)
$exposeGlobally!(ElementEventForPlaylist)
