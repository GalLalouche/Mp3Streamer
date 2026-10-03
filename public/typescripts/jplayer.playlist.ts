// FIXME merge with the customizations, and remove
/*
 * Playlist Object for the jPlayer Plugin http://www.jplayer.org Copyright (c)
 * 2009 - 2011 Happyworm Ltd Dual licensed under the MIT and GPL licenses. -
 * http://www.opensource.org/licenses/mit-license.php -
 * http://www.gnu.org/copyleft/gpl.html Author: Mark J Panaghiston Version:
 * 2.1.0 (jPlayer 2.1.0) Date: 1st September 2011
 */

/* Code verified using http://www.jshint.com/ */
/*
 * jshint asi:false, bitwise:false, boss:false, browser:true, curly:true,
 * debug:false, eqeqeq:true, eqnull:false, evil:false, forin:false, immed:false,
 * jquery:true, laxbreak:false, newcap:true, noarg:true, noempty:true,
 * nonew:true, nomem:false, onevar:false, passfail:false, plusplus:false,
 * regexp:false, undef:true, sub:false, strict:false, white:false
 */

/* global jPlayerPlaylist: true, jQuery:false, alert:false */

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

export class JPlayerPlaylist {
  playlist: Song[]
  private readonly player: Player
  current: number
  private removing: boolean

  private constructor(playlist: Song[], player: Player) {
    this.playlist = playlist
    this.player = player
    this.current = 0
    this.removing = false
  }

  static create(playlist: Song[], player: Player): JPlayerPlaylist {
    const result = new JPlayerPlaylist(playlist, player)

    result._init()
    player.listen((e: PlayerEvent) => {
      if (e === "ENDED")
        result.next()
    })
    return result;
  }

  _getDisplayedIndex(index: number): number {
    return this.playlist.length - 1 - index
  }
  _init(instant: boolean = false) {
    const self = this
    if (instant) {
      this._refresh(true)
      self.play(self.current)
    } else {
      this._refresh(function () {
        return self.play(self.current)
      })
    }
  }
  _initPlaylist(playlist: Song[]) {
    this.current = 0
    this.removing = false
    this.playlist = $.extend(true, [], playlist);
  }
  _refresh(instant: boolean | undefined | (() => unknown)) {
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
        playlistUl.append(self._createListItem(v))
      })
    } else {
      const $this = $(this)
      $this.empty()

      self.playlist.forEach(function (v) {
        $this.append(self._createListItem(v))
      })
      if (isFunction(instant))
        instant()
      if (self.playlist.length)
        $this.slideDown(DISPLAY_TIME)
      else
        $this.show()
    }
  }
  _createListItem(song: Song): JQuery<HTMLElement> {
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
// Temp hack
  removeItemAux(index: number, nextFunction: (e: JQuery<HTMLElement>) => JQuery<HTMLElement>) {
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

    aux($(`${PLAYLIST} li:nth-child(${this._getDisplayedIndex(index) + 1})`))
  }
//     // Create .live() handlers for the remove controls
//     GuiEvents.listen(PlaylistEventTopic, event => {
//           if (typeof event !== "object" || !("index" in event) || event.type === "select")
//             return // This is already covered elsewhere
//           function getNextFunction() {
//             if (trigger.hasClass(options.removeThisClass)) return _ => $()
//             if (trigger.hasClass(options.removeUpClass)) return x => x.prev()
//             assert(trigger.hasClass(options.removeDownClass))
//             return x => x.next()
//           }
//
//           function removeItemAux(nextFunction, who) {
//             // This has to be calculated before the removal, otherwise the who element is empty
//             const next = nextFunction(who)
//             self.remove(who.index(), function () {
//               // if there is another next element to remove,
//               // enqueue a removal after this current element is removed
//               if (next.length > 0)
//                 removeItemAux(nextFunction, next)
//             })
//           }
//         })
//         && event.type === "x"
// )
//   {
//     self.remove()
//   }
//   $(playlistSelector).on("click", "a." + this.options.playlistOptions.removeItemClass, function () {
//     const trigger = $(this)
//
//
//     removeItemAux(getNextFunction(), trigger.closest("li"))
//     return false
//   })
// },
//   _updateControls() {
//     const controls = $(`${this.cssSelector.playlist} .${this.options.playlistOptions.removeItemClass}`)
//     if (this.options.playlistOptions.enableRemoveControls)
//       controls.show()
//     else
//       controls.hide()
//   }
  _highlight(index: number) {
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
  async setPlaylist(playlist: Song[], instant: boolean) {
    this._initPlaylist(playlist)
    await this._init(instant)
  }
  add(song: Song | Song[], playNow: boolean = false) {
    const self = this
    if ($.isArray(song)) {
      song.forEach(x => self.add(x))
      return Promise.resolve()
    }
    if (this.playlist.some(e => e.file === song.file)) {
      console.log(`Entry ${song.file} already exists in playlist; skipping`)
      return Promise.resolve()
    }
    const playlistUl = $(PLAYLIST + " ul")
    playlistUl.prepend(this._createListItem(song))
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
  remove(index: number | undefined, onEnd: () => void) {
    const self = this

    if (index === undefined) {
      this._initPlaylist([])
      this._refresh(function () {
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
        const playlistIndex = self._getDisplayedIndex(index)
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
    const displayIndex = this._getDisplayedIndex(index)
    if (index < this.playlist.length) {
      this.current = index
      this._highlight(displayIndex)
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
  isLastSongPlaying(): boolean {
    return this.current === this.playlist.length - 1
  }
  currentPlayingSong(): Song {
    return this.playlist [this.current]
  }


// // Flag is true during remove animation, disabling the remove() method until complete.
//
// this.cssSelector = $.extend({}, this._cssSelector, cssSelector); // Object:
// // Containing the css selectors for jPlayer and its cssSelectorAncestor
// this.options = $.extend(true, {}, this._options, options); // Object:
// // The jPlayer constructor/ options for this playlist and the playlist options
//
// this.playlist = []; // Array of Objects: The current playlist displayed
// this._initPlaylist(playlist)
//
// // Setup the css selectors for the extra interface items used by the playlist.
// // Note that the text is written to the descendant li node.
// const append = s => `${this.cssSelector.cssSelectorAncestor} .${s}`
//
// // Override the cssSelectorAncestor given in options
// this.options.cssSelectorAncestor = this.cssSelector.cssSelectorAncestor
//
// // FIXME this should be made to work my player implementation. Starting with ready.
// // Create a ready event handler to initialize the playlist
//
// // Remove the empty <li> from the page HTML.
// // Allows page to be valid HTML, while not interfering with display animations
// $(this.cssSelector.playlist + " ul").empty()
// //
// // Instance jPlayer
// // $(this.cssSelector.jPlayer).jPlayer(this.options)
// }
//
//
// JPlayerPlaylist.prototype = {
//   _cssSelector: { // static object, instanced in constructor
//     jPlayer: "#jquery_jplayer_1",
//     cssSelectorAncestor: "#jp_container_1"
//   },
//   _options: { // static object, instanced in constructor
//     playlistOptions: {
//     }
//   },
}
