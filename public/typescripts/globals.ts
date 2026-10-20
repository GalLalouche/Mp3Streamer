import {Startup} from "./events.js"
import {FadePlayer} from "./fade_player.js"
import {Song} from "./media.js"
import {makePlayer} from "./player_singleton.js"
import {Playlist} from "./playlist.js"
import {makePlaylist} from "./playlist_singleton.js"
import * as WirePlayer from "./wire_player_events.js"
import * as WirePlaylist from "./wire_playlist_events.js"

export let gplayer!: FadePlayer
export let gplaylist!: Playlist

function globalAux(x: any): any {
  if (isNullable(x))
    throw new Error("gplayer and playlist aren't initialized yet; use GuiEvents.listen(PlayerSetup, ...) instead")
  return x
}

Object.defineProperty(globalThis, "gplayer", {get: () => globalAux(gplayer)})
Object.defineProperty(globalThis, "gplaylist", {get: () => globalAux(gplaylist)})

export async function addAndPlayNow(song: Song | Song[]): Promise<void> {
  await addAndSelect(song)
  await gplayer.playCurrentSong()
}

export async function addAndFade(song: Song): Promise<void> {
  if (gplayer.isFading().isFalse()) {
    gplayer.fadeOnNextLoad()
    return addAndSelect(song)
  } else {
    return gplaylist.add(song).void()
  }
}

async function addAndSelect(song: Song | Song[]): Promise<void> {
  const index = await gplaylist.add(song)
  await gplaylist.select(index)
}

$(function () {
  gplayer = makePlayer()
  gplaylist = makePlaylist()

  // Wiring has to finish before we resolve the startup events, since some clients fire off events
  // On setup (e.g., initialization)
  WirePlayer.wire(gplayer)
  WirePlaylist.wire(gplaylist)
  Startup.resolve({player: gplayer, playlist: gplaylist})
})
