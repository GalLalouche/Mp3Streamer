// FIXME merge with the customizations, and remove
/*
 * Adapted from http://www.jplayer.org playlist.
 */

import {match} from "ts-pattern";
import * as ColorUtils from "./color-utils.js"
import * as Local from "./local.js";
import {Song} from "./media.js";
import {gplayer} from "./player_singleton.js";
// FIXME cyclic dependency is only temporary since both files will be merged eventually
import * as PlaylistCustomizations from "./playlist_customizations.js";
import {Player, PlayerEvent} from "./types.js";

const REMOVE_ITEM = "jp-playlist-item-remove"
// FIXME this is reused in GuiEvents
const REMOVE_THIS = "jp-playlist-item-remove-this"
const REMOVE_UP = "jp-playlist-item-remove-up"
const REMOVE_DOWN = "jp-playlist-item-remove-down"
const ADD_TIME = 'fast'
const REMOVE_TIME = 'fast'
export const ITEM_CLASS = "jp-playlist-item"
const TITLE = ".jp-title"
const PLAYLIST = ".jp-playlist"
const DISPLAY_TIME = 'slow'

type RemoveFunction = (e: JQuery<HTMLElement>) => JQuery<HTMLElement>

export class JPlayerPlaylist {
  playlist: Song[]
  current: number
  private removing: boolean

  private constructor() {
    this.playlist = []
    this.current = 0
    this.removing = false
  }

  static create(player: Player): JPlayerPlaylist {
    const result = new JPlayerPlaylist()

    result.init(false)
    player.listen((e: PlayerEvent) => {
      if (e === "ENDED")
        result.next()
    })
    return result;
  }

  private getDisplayedIndex(index: number): number {
    return this.playlist.length - 1 - index
  }
  private init(instant: boolean): void {
    const self = this
    if (instant) {
      this.refresh(true)
      this.play(self.current)
    } else {
      this.refresh(function () {
        return self.play(self.current)
      })
    }
  }
  private initPlaylist(playlist: Song[]) {
    this.current = 0
    this.removing = false
    this.playlist = $.extend(true, [], playlist);
  }
  private refresh(instant: boolean | undefined | (() => unknown)) {
    /*
     * instant: Can be undefined, true or a function.
     *  undefined -> use animation timings
     *  true -> no animation
     *  function -> use animation timings and execute function at half way point.
     */
    const self = this

    const playlistUl = $(PLAYLIST + " ul")
    if (instant && not(isFunction(instant))) {
      playlistUl.empty()
      this.playlist.forEach(function (v) {
        playlistUl.append(self.createListItem(v))
      })
    } else {
      const $this = $(this)
      $this.empty()

      self.playlist.forEach(function (v) {
        $this.append(self.createListItem(v))
      })
      if (isFunction(instant))
        instant()
      if (self.playlist.length)
        $this.slideDown(DISPLAY_TIME)
      else
        $this.show()
    }
  }
  private createListItem(song: Song): JQuery<HTMLElement> {
    let listItem = "<li><div>"

    function appendIcon(clazz: string, char: string) {
      listItem += `<a href='javascript:;' class='${REMOVE_ITEM} ${clazz}'>${char}</a>`
    }

    // The title is given next in the HTML otherwise the float:right on the free media corrupts in IE6/7
    listItem += PlaylistCustomizations.mediaMetadataHtml(song)
    appendIcon(REMOVE_THIS, "&times;")
    appendIcon(REMOVE_UP, "&uparrow;")
    appendIcon(REMOVE_DOWN, "&downarrow;")
    listItem += "</div></li>"

    const result = $(listItem)
    result.prepend(img(song.poster).addClass("playlist-item-poster"))
    ColorUtils.getColor(song.poster).then(rgb => {
      result.css('background-color', rgb.makeLighter(0.1).toString())
    })
    return result
  }

  removeItem(index: number, type: "x" | "up" | "down") {
    this.removeItemAux(index, match(type)
      .returnType<RemoveFunction>()
      .with("x", () => () => $())
      .with("up", () => x => x.prev())
      .with("down", () => x => x.next())
      .exhaustive())
  }

  private removeItemAux(index: number, nextFunction: RemoveFunction) {
    const self = this

    function aux(current: JQuery<HTMLElement>) {
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
  private highlight(index: number) {
    if (this.playlist.length && index !== undefined) {
      $(`${PLAYLIST} .jp-playlist-current`).removeClass("jp-playlist-current")
      $(`${PLAYLIST} li:nth-child(${index + 1})`).addClass("jp-playlist-current")
        .find(".jp-playlist-item").addClass("jp-playlist-current")
      $(`${TITLE} li`).html(
        this.playlist[index].title
        + (this.playlist[index].artistName ? " <span class='jp-artist'>by "
          + this.playlist[index].artistName + "</span>" : ""))
    }
  }
  async setPlaylist(playlist: Song[]) {
    this.initPlaylist(playlist)
    this.init(true)
  }
  add(song: Song | Song[], playNow: boolean = false) {
    const self = this
    if (Array.isArray(song)) {
      song.forEach(x => self.add(x))
      return Promise.resolve()
    }
    if (this.playlist.some(e => e.file === song.file)) {
      console.log(`Entry ${song.file} already exists in playlist; skipping`)
      return Promise.resolve()
    }
    const playlistUl = $(PLAYLIST + " ul")
    playlistUl.prepend(this.createListItem(song))
      .find("li:first-child").hide()
      .slideDown(ADD_TIME, function () {
        const regularHeightThreshold = 30
        const lastSong = playlistUl.find("li:first-child")
        if (lastSong.height()! > regularHeightThreshold)
          console.log("too big, need to shorten")
      })
    this.playlist.push(song)

    if (playNow)
      return this.play(this.playlist.length - 1)
    else if (this.playlist.length === 1)
      return this.select(0)
    else
      return Promise.resolve()
  }
  private remove(index: number | undefined, onEnd: () => void) {
    const self = this

    if (index === undefined) {
      this.initPlaylist([])
      this.refresh(function () {
        gplayer.clear()
      })
      return true
    }
    if (this.removing)
      return false
    if (index < 0)
      return arguments.callee(self.playlist.length + index, onEnd)
    // index relates to end of array.
    if (index < this.playlist.length)
      this.removing = true

    $(`${PLAYLIST} li:nth-child(${index + 1})`).slideUp(
      REMOVE_TIME,
      function () {
        $(this).remove()
        const playlistIndex = self.getDisplayedIndex(index)
        self.playlist.splice(playlistIndex, 1)
        if (self.playlist.length) {
          if (playlistIndex === self.current) {
            // Update current when last element was deleted.
            self.current = playlistIndex < self.playlist.length ? self.current : self.playlist.length - 1
            self.select(self.current)
          } else if (playlistIndex < self.current)
            self.current--
        } else {
          gplayer.clear()
          self.current = 0
        }

        self.removing = false
        if (onEnd)
          onEnd()
      })
    return true
  }
  async select(index: number): Promise<void> {
    if (index < 0)
      return arguments.callee(this.playlist.length + index)
    // index relates to end of array.
    const displayIndex = this.getDisplayedIndex(index)
    if (index < this.playlist.length) {
      this.current = index
      this.highlight(displayIndex)
      return Local.maybePreLoad(this.playlist[this.current]).then(e => gplayer.load(e))
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
  next(): void {
    if (this.isLastSongPlaying())
      return notImplemented()
    const index = (this.current + 1 < this.playlist.length) ? this.current + 1 : 0
    if (index > 0)
      this.play(index)
  }
  previous(): void {
    const index = (this.current - 1 >= 0) ? this.current - 1 : this.playlist.length - 1
    if (index < this.playlist.length - 1)
      this.play(index)
  }
  private isLastSongPlaying(): boolean {
    return this.current === this.playlist.length - 1
  }
}
