// FIXME merge with the customizations, and remove
/* Adapted from http://www.jplayer.org playlist. */

import {match} from "ts-pattern"
import * as DataApi from "./data_api.js"
import * as Local from "./local.js"
import {Song} from "./media.js"
import {gplayer} from "./player_singleton.js"
import {PlayerEvent, TimeUpdate} from "./player.js"
import {Player} from "./player.js"
import {Duration} from "./common_types.js"
import {PlaylistItem} from "./playlist_item.js"


const ADD_TIME = 'fast'
const REMOVE_TIME = 'fast'
const TITLE = ".jp-title"
const PLAYLIST = ".jp-playlist"
const DISPLAY_TIME = 'slow'
const PLAYLIST_CURRENT = "jp-playlist-current"

type RemoveFunction = (e: JQuery<HTMLElement>) => JQuery<HTMLElement>
const PRELOAD_GAP = Duration.fromSeconds(20)

export class JPlayerPlaylist {
  private playlist: PlaylistItem[]
  private current: number
  private removing: boolean

  private constructor() {
    this.playlist = []
    this.current = 0
    this.removing = false
  }

  currentIndex(): number {
    return this.current
  }

  songs(): readonly Song[] {
    return this.playlist.map(e => e.song)
  }
  length(): number {
    return this.playlist.length
  }
  getSong(index: number): Song {
    return this.playlist[index].song
  }

  static create(player: Player): JPlayerPlaylist {
    const result = new JPlayerPlaylist()

    result.init(false)
    player.listen((e: PlayerEvent) => {
      if (e === "ENDED")
        result.next()
      else if (e instanceof TimeUpdate) {
        result.maybePreloadNextSong(e)
      }
    })
    return result
  }

  async setPlaylist(playlist: readonly Song[]): Promise<void> {
    this.initPlaylist(playlist)
    this.init(true)
  }

  async add(song: Song | readonly Song[], playNow: boolean = false): Promise<void> {
    if (Array.isArray(song)) {
      for (const s of song)
        await this.add(s)
      return Promise.resolve()
    }
    if (this.playlist.some(e => e.song.file === song.file)) {
      console.log(`Entry ${song.file} already exists in playlist; skipping`)
      return Promise.resolve()
    }
    const playlistUl = $(PLAYLIST + " ul")
    const item = PlaylistItem.create(song)
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
    else if (this.playlist.length === 1)
      return this.select(0)
    else
      return Promise.resolve()
  }

  removeItem(index: number, type: "x" | "up" | "down"): void {
    this.removeItemAux(index, match(type)
      .returnType<RemoveFunction>()
      .with("x", () => () => $())
      .with("up", () => x => x.prev())
      .with("down", () => x => x.next())
      .exhaustive())
  }

  private remove(index: number, onEnd: () => void): void {
    if (this.removing)
      return
    if (index < 0)
      return this.remove(this.playlist.length + index, onEnd)
    // index relates to end of array.
    if (index < this.playlist.length)
      this.removing = true

    $(`${PLAYLIST} li:nth-child(${index + 1})`).slideUp(
      REMOVE_TIME,
      () => {
        $(this).remove()
        const playlistIndex = this.getDisplayedIndex(index)
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

  async select(index: number): Promise<void> {
    if (index < 0)
      return arguments.callee(this.playlist.length + index)
    // index relates to end of array.
    if (index < this.playlist.length) {
      this.current = index
      this.markCurrent()
      return Local.maybePreLoad(this.getSong(this.current)).then(e => gplayer.load(e))
    } else {
      this.current = 0
      return new Promise(f => f())
    }
  }

  async play(index: number): Promise<void> {
    if (index < 0)
      return arguments.callee(this.playlist.length + index)
    // index relates to end of array.
    if (index < this.playlist.length && this.playlist.length)
      return this.select(index).then(() => gplayer.playCurrentSong())
  }

  async next(): Promise<void> {
    // This can happen when next is invoke manually. In normal operation, the next song pre-loaded
    // when the current song is about to end.
    return this.isLastSongPlaying()
      ? DataApi.getRandomSong().then(song => this.add(song, true))
      : this.play(this.current + 1)
  }

  previous(): void {
    const index = (this.current - 1 >= 0) ? this.current - 1 : this.playlist.length - 1
    if (index < this.playlist.length - 1)
      this.play(index)
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
    this.playlist = playlist.map(PlaylistItem.create)
  }

  private refresh(animation?: () => void): void {
    if (animation) {
      const $this = $(this)
      $this.empty()

      this.playlist.forEach(v => $this.append(v.element))
      animation()
      if (this.playlist.length)
        $this.slideDown(DISPLAY_TIME)
      else
        $this.show()
    } else {
      const playlistUl = $(PLAYLIST + " ul")
      playlistUl.empty()
      this.playlist.forEach(v => playlistUl.append(v.element))
    }
  }

  private removeItemAux(index: number, nextFunction: RemoveFunction): void {
    const self = this

    function aux(current: JQuery<HTMLElement>): void {
      const next = nextFunction(current)
      // This has to be calculated before the removal, otherwise the who element is empty
      self.remove(current.index(), function () {
        // if there is another next element to remove,
        // enqueue a removal after this current element is removed
        if (next.length > 0)
          aux(next)
      })
    }

    aux($(`${PLAYLIST} li:nth-child(${this.getDisplayedIndex(index) + 1})`))
  }

  private isLastSongPlaying(): boolean {
    return this.current === this.playlist.length - 1
  }

  maybePreloadNextSong(tu: TimeUpdate): Promise<void> {
    return (
      this.isLastSongPlaying() && PRELOAD_GAP.isGreaterThanOrEqual(tu.remainingDuration())
        ? DataApi.getRandomSong().then(song => this.add(song, false))
        : Promise.resolve()
    )
  }

  private markCurrent(): void {
    const displayedIndex = this.getDisplayedIndex(this.current)
    $(`${PLAYLIST} .${PLAYLIST_CURRENT}`).removeClass(PLAYLIST_CURRENT)
    // FIXME we probably don't really need to the class to two different elements here.
    // FIXME Duplication of nth-child selector with above.
    $(`${PLAYLIST} li:nth-child(${displayedIndex + 1})`).addClass(PLAYLIST_CURRENT)
      .find(".jp-playlist-item").addClass(PLAYLIST_CURRENT)
    const song = this.getSong(displayedIndex)
    $(`${TITLE} li`).html(
      // FIXME there has to be a nicer way of doing this.
      song.title + ` <span class='jp-artist'>by ${song.artistName}</span>`
    )
  }

  private getDisplayedIndex(index: number): number {return this.playlist.length - 1 - index}
}

