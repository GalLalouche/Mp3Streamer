import {Duration, Percentage, Player, Song, Volume} from "./types.js";
import {PlayerImpl} from "./player.js";
import {getSearchParam} from "./initialization.js";


interface JPlayerElement {
  jPlayer(str: String, value: any): void
  data(): any
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
      override playCurrentSong(): void {this.click("play")}
      override isPaused(): boolean {return this.player().data().jPlayer.status.paused}
      override percentageOfSongPlayed() {
        const jPlayer = this.player().data().jPlayer
        return jPlayer ?
          Percentage.fromMax100(jPlayer.status.currentPercentAbsolute) :
          Percentage.fromMax1(0)
      }
      override currentPlayingInSeconds(): Duration {
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
    };
}

$(function () {
  gplayer = extracted();
})
$exposeGlobally!(gplayer)
