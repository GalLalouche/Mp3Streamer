import {Duration, Percentage} from "./common_types.js";
import {GuiEvents, PlayerControls, PlayerControlsTopic} from "./gui_events.js";
import {HtmlPlayer} from "./html_player";
import {JPlayerPlaylist} from "./jplayer.playlist.js";
import {Song} from "./media.js";
import * as PlayerGUI from "./player_gui.js";
import {Player, PlayerEvent, Playlist, TimeUpdate, Volume} from "./types";
import * as VolumeSetter from "./volume_setter.js";

// TODO temporary, until this is refactored to use a proper singleton method.
export let gplayer!: Player
export let gplaylist!: Playlist

class SingletonPlayer extends Player {
  private readonly player: Player

  private constructor(player: Player) {
    super()
    this.player = player
  }
  static from(player: Player): SingletonPlayer {
    const result = new SingletonPlayer(player)
    result.listen((pe: PlayerEvent) => {
      if (pe instanceof TimeUpdate) {
        PlayerGUI.updatePosition({
          current: result.currentTime(),
          total: result.duration()
        })
        PlayerGUI.setIsPlaying()
      }
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

function makePlaylist(player: Player): Playlist {
  const pl: JPlayerPlaylist = JPlayerPlaylist.create(player)

  return new class extends Playlist {
    override currentIndex() {return pl.currentIndex()}
    override songs() {return pl.songs()}
    override add(song: Song | readonly Song[], playNow: boolean): Promise<void> {
      return pl.add(song, playNow)
    }
    override _next() {return pl.next()}
    override prev() {return pl.previous()}
    override async clear(): Promise<void> {return pl.setPlaylist([])}
    override play(index: number): Promise<void> { return pl.play(index)}
    override select(index: number): Promise<void> {return pl.select(index)}
    override removeItem(index: number, type: "x" | "up" | "down") {pl.removeItem(index, type)}
  }
}

$(function () {
  gplayer = SingletonPlayer.from(HtmlPlayer.create())
  gplayer.setVolume(VolumeSetter.getVolumeBaseline())
  gplaylist = makePlaylist(gplayer)
})
