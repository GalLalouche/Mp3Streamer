/* Adapted from http://www.jplayer.org playlist. */

import {match, P} from "ts-pattern"
import {Events, PlayerControls} from "./events.js"
import {Song} from "./media.js"
import {PlaylistItem, RemoveType} from "./playlist_item.js"


/**
 * Handles the playlist via an HTML list.
 *
 * Also handles listening to song changes via clicks on the song elements, item removals. If the selection has changed, will use {@link Events} to publish under the {@link PlayerControls}.
 *
 * Does not handle the actual playing of songs, which is handled by the Player class.
 */
export class HtmlPlaylist {
  private playlist: PlaylistItem[] = []
  private current: number | null = null
  private removing: boolean = false

  static create(): HtmlPlaylist {
    const result = new HtmlPlaylist()
    result.refresh()
    return result
  }

  currentIndex(): number {
    return definedOrThrow(this.current)
  }
  songs(): readonly Song[] {return this.playlist.map(e => e.song)}
  length(): number {return this.playlist.length}
  getSong(index: number): Song {return this.playlist[index].song}

  async setPlaylist(playlist: readonly Song[]): Promise<void> {
    this.initPlaylist(playlist)
    this.refresh()
  }

  add(song: Song, mode: 'animate' | 'instant'): void {
    if (this.playlist.some(e => e.song.file === song.file)) {
      console.log(`Entry ${song.file} already exists in playlist; skipping`)
      return
    }
    const playlistUl = $(PLAYLIST + " ul")
    const item = this.createItem(song)
    const duration = match(mode).with("animate", () => 'fast').with("instant", () => 0).exhaustive()
    playlistUl.prepend(item.element)
      .find("li:first-child").hide()
      .slideDown(duration, function () {
        const regularHeightThreshold = 30
        const lastSong = playlistUl.find("li:first-child")
        if (lastSong.height()! > regularHeightThreshold)
          console.log("too big, need to shorten")
      })
    this.playlist.push(item)

    if (this.current === null)
      this.select(0)
  }

  /** Returns true iff the *current* song was removed. */
  removeItem(index: number, type: RemoveType): boolean {
    const willDeleteCurrent =
      index === this.currentIndex() ||
      (index < this.currentIndex() && type === "down") ||
      (index > this.currentIndex() && type === "up")
    this.removeItemAux(index, match(type)
      .returnType<RemoveFunction>()
      .with("x", () => () => $())
      .with("up", () => x => x.prev())
      .with("down", () => x => x.next())
      .exhaustive())
    return willDeleteCurrent
  }

  select(index: number): void {
    if (this.current !== null && this.current === index)
      return
    this.current = index
    this.markCurrent()
    const song = this.getSong(this.current)
    Events.publish(PlayerControls, song)
  }

  next(): void {
    // This can happen when next is invoked manually. In normal operation, the next song is
    // pre-loaded when the current song is about to end.
    require(this.current !== null && this.isLastSongPlaying().isFalse())
    this.select(this.current + 1)
  }

  previous(): void {
    if (this.current !== null && this.current > 0)
      this.select(this.current - 1)
  }

  // This works with elements instead of indices, since the indices shift on removal, but going
  // from an element to its preceeding or succeeding element is easier.
  private remove(elem: JQuery<HTMLElement>, onEnd: () => void): void {
    if (this.removing)
      return

    this.removing = true
    // The list is displayed in reverse order, i.e., the last playlist element is in the top of the <ul>.
    const playlistIndex = this.getDisplayedIndex(elem)
    elem.slideUp(REMOVE_TIME, () => {
      elem.remove()
      this.playlist.splice(playlistIndex, 1)
      if (this.playlist.custom_nonEmpty()) {
        if (playlistIndex === this.current)
          // Update current when current playing element was deleted.
          this.select(playlistIndex < this.playlist.length ? this.current : this.playlist.length - 1)
        else if (playlistIndex < this.currentIndex())
          this.current!--
      } else {
        this.current = 0
      }

      this.removing = false
      if (onEnd)
        onEnd()
    })
  }

  private initPlaylist(playlist: readonly Song[]): void {
    this.current = 0
    this.removing = false
    this.playlist = playlist.map(e => this.createItem(e))
  }

  private createItem(song: Song): PlaylistItem {
    const result = PlaylistItem.create(song)
    result.listen(type => {
      const index = this.getDisplayedIndex(result.element)
      match(type)
        .with("select", () => this.select(index))
        .with(P.select(), x => this.removeItem(index, x))
    })
    return result
  }

  private refresh(): void {
    const playlistUl = $(PLAYLIST + " ul")
    playlistUl.empty()
    this.playlist.forEach(v => playlistUl.append(v.element))
  }

  private removeItemAux(index: number, nextFunction: RemoveFunction): void {
    const self = this

    function aux(current: JQuery<HTMLElement>): void {
      // This has to be calculated before the removal, otherwise the current element is empty
      const next = nextFunction(current)
      self.remove(current, function () {
        // if there is another next element to remove,
        // enqueue a removal after this current element is removed
        if (next.custom_nonEmpty())
          aux(next)
      })
    }

    aux(this.playlist[index].element)
  }

  isLastSongPlaying(): boolean {return this.current === this.playlist.length - 1}

  private markCurrent(): void {
    $(`${PLAYLIST} .${PLAYLIST_CURRENT}`).removeClass(PLAYLIST_CURRENT)
    this.playlist[this.currentIndex()].element.addClass(PLAYLIST_CURRENT)
  }

  private getDisplayedIndex(e: JQuery<HTMLElement>): number {
    return this.playlist.length - 1 - e.index()
  }
}

const REMOVE_TIME = 'fast'
const PLAYLIST = ".jp-playlist"
const PLAYLIST_CURRENT = "jp-playlist-current"

type RemoveFunction = (e: JQuery<HTMLElement>) => JQuery<HTMLElement>
