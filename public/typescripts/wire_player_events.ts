/** Processes events and wires them to the playlist. */
import {match, P} from "ts-pattern"
import {Events, PlayerControl, PlayerControls, Seek} from "./events.js"
import {maybePreLoad} from "./local.js"
import {Song} from "./media.js"
import {Player} from "./player.js"
import {Volume} from "./volume.js"

export function wire(player: Player) {
  Events.listen(PlayerControls, (control: PlayerControl) => {
    match(control)
      .with("play", () => player.playCurrentSong())
      .with("stop", () => player.stop())
      .with("pause", () => player.pause())
      .with("toggle_pause", () => {
        if (player.isPaused())
          player.playCurrentSong()
        else
          player.pause()
      })
      .with(P.instanceOf(Volume), v => player.setVolume(v))
      .with(P.instanceOf(Seek), s => player.skipTo(player.duration().times(s.percentage.zeroToOne())))
      .with(P.instanceOf(Song), async s => {
        const wasPlaying = player.isPaused().isFalse() || player.hasEnded()
        await maybePreLoad(s)
        player.load(s)
        if (wasPlaying)
          await player.playCurrentSong()
      })
      .exhaustive()
  })
}
