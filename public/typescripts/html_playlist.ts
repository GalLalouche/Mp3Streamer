/* Adapted from http://www.jplayer.org playlist. */

import {match, P} from "ts-pattern"
import * as Local from "./local.js"
import {Song} from "./media.js"
import {gplayer} from "./globals.js"
import {PlaylistItem, RemoveType} from "./playlist_item.js"


/**
 * Handles the playlist via an HTML list.
 *
 * Also handles listening to song changes via clicks on the song elements, item removals, and
 * pre-loading the next random song when the current song is about to end and it's the last one.
 */
export class HtmlPlaylist {
  private playlist: PlaylistItem[]
  private current: number
  private removing: boolean

  private constructor() {
    this.playlist = []
    this.current = 0
    this.removing = false
  }

  static create(): HtmlPlaylist {
    const result = new HtmlPlaylist()
    result.init(false)
    return result
  }

  currentIndex(): number {return this.current}
  songs(): readonly Song[] {return this.playlist.map(e => e.song)}
  length(): number {return this.playlist.length}
  getSong(index: number): Song {return this.playlist[index].song}

  async setPlaylist(playlist: readonly Song[]): Promise<void> {
    this.initPlaylist(playlist)
    this.init(true)
  }

  async add(song: Song | readonly Song[], playNow: boolean = false): Promise<void> {
    if (Array.isArray(song)) {
      for (const s of song)
        await this.add(s)
      return
    }
    if (this.playlist.some(e => e.song.file === song.file)) {
      console.log(`Entry ${song.file} already exists in playlist; skipping`)
      return
    }
    const playlistUl = $(PLAYLIST + " ul")
    const item = this.createItem(song)
    playlistUl.prepend(item.element)
      .find("li:first-child").hide()
      .slideDown(ADD_TIME, function () {
        const regularHeightThreshold = 30
        const lastSong = playlistUl.find("li:first-child")
        if (lastSong.height()! > regularHeightThreshold)
          console.log("too big, need to shorten")
      })
    this.playlist.push(item)

    if (playNow)
      return this.play(this.playlist.length - 1)
    if (this.playlist.length === 1)
      return this.select(0)
  }

  removeItem(index: number, type: RemoveType): void {
    this.removeItemAux(index, match(type)
      .returnType<RemoveFunction>()
      .with("x", () => () => $())
      .with("up", () => x => x.prev())
      .with("down", () => x => x.next())
      .exhaustive())
  }

  async select(index: number): Promise<void> {
    if (index < 0)
      return arguments.callee(this.playlist.length + index)
    // index relates to end of array.
    if (index < this.playlist.length) {
      this.current = index
      this.markCurrent()
      return Local.maybePreLoad(this.getSong(this.current)).then(e => gplayer.load(e))
    }

    this.current = 0
  }

  async play(index: number): Promise<void> {
    if (index < 0)
      return arguments.callee(this.playlist.length + index)
    // index relates to end of array.
    if (index < this.playlist.length && this.playlist.length)
      return this.select(index).then(() => gplayer.playCurrentSong())
  }

  /** Returns true iff there is a next song to load. */
  next(): boolean {
    // This can happen when next is invoke manually. In normal operation, the next song pre-loaded
    // when the current song is about to end.
    if (this.isLastSongPlaying())
      return false

    this.play(this.current + 1)
    return true
  }

  previous(): void {
    const index = (this.current - 1 >= 0) ? this.current - 1 : this.playlist.length - 1
    if (index < this.playlist.length - 1)
      this.play(index)
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
      if (this.playlist.length) {
        if (playlistIndex === this.current) {
          // Update current when last element was deleted.
          this.current = playlistIndex < this.playlist.length ? this.current : this.playlist.length - 1
          this.select(this.current)
        } else if (playlistIndex < this.current)
          this.current--
      } else {
        gplayer.clear()
        this.current = 0
      }

      this.removing = false
      if (onEnd)
        onEnd()
    })
  }

  private init(instant: boolean): void {
    if (instant) {
      this.refresh()
      this.play(this.current)
    } else {
      this.refresh(() => this.play(this.current))
    }
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

  private refresh(animation?: () => void): void {
    const playlistUl = $(PLAYLIST + " ul")
    if (animation) {
      playlistUl.empty()

      this.playlist.forEach(v => playlistUl.append(v.element))
      animation()
      if (this.playlist.length)
        playlistUl.slideDown(DISPLAY_TIME)
      else
        playlistUl.show()
    } else {
      playlistUl.empty()
      this.playlist.forEach(v => playlistUl.append(v.element))
    }
  }

  private removeItemAux(index: number, nextFunction: RemoveFunction): void {
    const self = this

    function aux(current: JQuery<HTMLElement>): void {
      // This has to be calculated before the removal, otherwise the current element is empty
      const next = nextFunction(current)
      self.remove(current, function () {
        // if there is another next element to remove,
        // enqueue a removal after this current element is removed
        if (next.length > 0)
          aux(next)
      })
    }

    aux(this.playlist[index].element)
  }

  isLastSongPlaying(): boolean {return this.current === this.playlist.length - 1}

  private markCurrent(): void {
    $(`${PLAYLIST} .${PLAYLIST_CURRENT}`).removeClass(PLAYLIST_CURRENT)
    this.playlist[this.current].element.addClass(PLAYLIST_CURRENT)
  }

  private getDisplayedIndex(e: JQuery<HTMLElement>): number {
    return this.playlist.length - 1 - e.index()
  }
}

const ADD_TIME = 'fast'
const REMOVE_TIME = 'fast'
const PLAYLIST = ".jp-playlist"
const DISPLAY_TIME = 'slow'
const PLAYLIST_CURRENT = "jp-playlist-current"

type RemoveFunction = (e: JQuery<HTMLElement>) => JQuery<HTMLElement>

