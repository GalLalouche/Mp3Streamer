/** Processes events and wires them to the playlist. */
import {match, P} from "ts-pattern"
import {Events, PlayerControl, PlayerControls, Seek, Startup} from "./events.js"
import {Volume} from "./volume.js"
import {Song} from "./media.js"

$(function () {
  Startup.promise.then(async ({player}) => {
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
        .with(P.instanceOf(Song), s => {
          const wasPlaying = player.isPaused().isFalse()
          player.load(s)
          if (wasPlaying)
            player.playCurrentSong()
        })
        .exhaustive()
    })
  })
})
