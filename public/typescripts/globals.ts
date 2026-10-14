import {Startup} from "./events.js"
import {Song} from "./media.js"
import {Player} from "./player.js"
import {makePlayer} from "./player_singleton.js"
import {Playlist} from "./playlist.js"
import {makePlaylist} from "./playlist_singleton.js"

export let gplayer!: Player
export let gplaylist!: Playlist

function globalAux(x: any): any {
  if (isNullable(x))
    throw new Error("gplayer and playlist aren't initialized yet; use GuiEvents.listen(PlayerSetup, ...) instead")
  return x
}

Object.defineProperty(globalThis, "gplayer", {get: () => globalAux(gplayer)})
Object.defineProperty(globalThis, "gplaylist", {get: () => globalAux(gplaylist)})

export async function addAndPlayNow(song: Song | Song[]): Promise<void> {
  const index = await gplaylist.add(song)
  await gplaylist.select(index)
  await gplayer.playCurrentSong()
}

$(function () {
  gplayer = makePlayer()
  gplaylist = makePlaylist()
  Startup.resolve({player: gplayer, playlist: gplaylist})
})
