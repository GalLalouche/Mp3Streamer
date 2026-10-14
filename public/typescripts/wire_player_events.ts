/** Processes events and wires them to the playlist. */
import {match, P} from "ts-pattern"
import {Events, PlayerControls, PlayerControlsTopic, PlaylistEventTopic, Seek, Startup} from "./events.js"
import {Volume} from "./volume.js"
import {Song} from "./media.js"

$(function () {
  Startup.promise.then(async ({player}) => {
    Events.listen(PlayerControlsTopic, (control: PlayerControls) => {
      match(control)
        .with("play", () => player.playCurrentSong())
        .with("stop", () => player.stop())
        .with("pause", () => player.pause())
        .with(P.instanceOf(Volume), v => player.setVolume(v))
        .with(P.instanceOf(Seek), s => player.skipTo(player.duration().times(s.percentage.zeroToOne())))
        .exhaustive()
    })
    Events.listen(PlaylistEventTopic, event => {
      const wasPlaying = player.isPaused().isFalse()
      if (event instanceof Song) {
        player.load(event)
        if (wasPlaying)
          player.playCurrentSong()
      }
    })
  })
})
