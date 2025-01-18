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

(function($, undefined) {
  JPlayerPlaylist = function(cssSelector, playlist, options) {
    const self = this

    this.current = 0;
    // Flag is true during remove animation, disabling the remove() method until complete.
    this.removing = false;

    this.cssSelector = $.extend({}, this._cssSelector, cssSelector); // Object:
    // Containing the css selectors for jPlayer and its cssSelectorAncestor
    this.options = $.extend(true, {}, this._options, options); // Object:
    // The jPlayer constructor/ options for this playlist and the playlist options

    this.playlist = []; // Array of Objects: The current playlist displayed
    this._initPlaylist(playlist);

    // Setup the css selectors for the extra interface items used by the playlist.
    // Note that the text is written to the descendant li node.
    const append = s => `${this.cssSelector.cssSelectorAncestor} .${s}`
    this.cssSelector.title = append("jp-title")
    this.cssSelector.playlist = append("jp-playlist")
    this.cssSelector.next = append("jp-next")
    this.cssSelector.previous = append("jp-previous")

    // Override the cssSelectorAncestor given in options
    this.options.cssSelectorAncestor = this.cssSelector.cssSelectorAncestor;

    // Create a ready event handler to initialize the playlist
    $(this.cssSelector.jPlayer).bind($.jPlayer.event.ready, function() {
      self._init();
    });

    // Create an ended event handler to move to the next item
    $(this.cssSelector.jPlayer).bind($.jPlayer.event.ended, function() {
      self.next();
    });

    // Create a play event handler to pause other instances
    $(this.cssSelector.jPlayer).bind($.jPlayer.event.play, function() {
      $(this).jPlayer("pauseOthers");
    });

    // Create click handlers for the extra buttons that do playlist functions.
    $(this.cssSelector.previous).click(function() {
      self.previous();
      $(this).blur();
      return false;
    });

    $(this.cssSelector.next).click(function() {
      self.next();
      $(this).blur();
      return false;
    });

    // Remove the empty <li> from the page HTML.
    // Allows page to be valid HTML, while not interfering with display animations
    $(this.cssSelector.playlist + " ul").empty();

    // Create .live() handlers for the playlist items along with the free/ media and remove controls.
    this._createItemHandlers();

    // Instance jPlayer
    $(this.cssSelector.jPlayer).jPlayer(this.options);
  };

  JPlayerPlaylist.prototype = {
    _cssSelector: { // static object, instanced in constructor
      jPlayer: "#jquery_jplayer_1",
      cssSelectorAncestor: "#jp_container_1"
    },
    _options: { // static object, instanced in constructor
      playlistOptions: {
        autoPlay: true,
        enableRemoveControls: true,
        displayTime: 'slow',
        addTime: 'fast',
        removeTime: 'fast',
        itemClass: "jp-playlist-item",
        freeGroupClass: "jp-free-media",
        freeItemClass: "jp-playlist-item-free",
        removeItemClass: "jp-playlist-item-remove",
        removeThisClass: "jp-playlist-item-remove-this",
        removeUpClass: "jp-playlist-item-remove-up",
        removeDownClass: "jp-playlist-item-remove-down"
      }
    },
    _getDisplayedIndex: function(index) {
      return this.playlist.length - 1 - index;
    },
    option: function(option, value) { // For changing playlist options only.
      if (value === undefined)
        return this.options.playlistOptions[option];

      this.options.playlistOptions[option] = value;

      switch (option) {
        case "enableRemoveControls":
          this._updateControls();
          break;
        case "itemClass":
        case "freeGroupClass":
        case "freeItemClass":
        case "removeItemClass":
          this._refresh(true); // Instant
          this._createItemHandlers();
          break;
      }
      return this;
    },
    _init: function(instant) {
      const self = this
      if (instant) {
        this._refresh(true);
        self.play(self.current);
      } else {
        this._refresh(function() {
          if (self.options.playlistOptions.autoPlay)
            return self.play(self.current)
          else
            return self.select(self.current)
        });
      }
    },
    _initPlaylist: function(playlist) {
      this.current = 0;
      this.removing = false;
      this.playlist = $.extend(true, [], playlist); // Copy the Array of Objects
    },
    _refresh: function(instant) {
      /*
       * instant: Can be undefined, true or a function.
       *  undefined -> use animation timings
       *  true -> no animation
       *  function -> use animation timings and execute function at half way point.
       */
      const self = this

      const playlistUl = $(this.cssSelector.playlist + " ul")
      if (instant && not($.isFunction(instant))) {
        playlistUl.empty();
        this.playlist.forEach(function(v) {
          playlistUl.append(self._createListItem(v));
        })
        this._updateControls();
      } else {
        const displayTime = playlistUl.children().length
            ? this.options.playlistOptions.displayTime
            : 0

        playlistUl.slideUp(displayTime, function() {
          const $this = $(this)
          $this.empty();

          self.playlist.forEach(function(v) {
            $this.append(self._createListItem(v));
          });
          self._updateControls();
          if ($.isFunction(instant))
            instant();
          if (self.playlist.length)
            $this.slideDown(self.options.playlistOptions.displayTime);
          else
            $this.show();

        });
      }
    },
    _createListItem: function(media) {
      const self = this;

      // Wrap the <li> contents in a <div>
      let listItem = "<li><div>"

      const options = this.options.playlistOptions
      // Create remove controls
      function appendIcon(clazz, char) {
        listItem += `<a href='javascript:;' class='${options.removeItemClass} ${clazz}'>${char}</a>`;
      }

      // Create links to free media
      if (media.free) {
        let first = true;
        listItem += `<span class='${this.options.playlistOptions.freeGroupClass}'>(`;
        $.each(media, function(property, value) {
          if ($.jPlayer.prototype.format[property]) {
            // property is a media format.
            if (first)
              first = false;
            else
              listItem += " | ";
            listItem += `<a class='${self.options.playlistOptions.freeItemClass}' href='${value}' tabindex='1'>${property}</a>`;
          }
        });
        listItem += ")</span>";
      }

      this.mediaMetadataHtml = PlaylistCustomizations.mediaMetadataHtml
      // The title is given next in the HTML otherwise the float:right on the free media corrupts in IE6/7
      listItem += this.mediaMetadataHtml(media)
      appendIcon(options.removeThisClass, "&times;")
      appendIcon(options.removeUpClass, "&uparrow;")
      appendIcon(options.removeDownClass, "&downarrow;")
      listItem += "</div></li>";

      const result = $(listItem);
      result.prepend(img(media.poster).addClass("playlist-item-poster"))
      ColorGetter.getColor(media.poster, rgb => {
        result.css('background-color', rgb.makeLighter(0.1).toString())
      })
      return result;
    },
    _createItemHandlers: function() {
      const self = this;
      // Create .live() handlers for the playlist items
      const playlistSelector = self.cssSelector.playlist
      $(`${(playlistSelector)} a.${this.options.playlistOptions.itemClass}`).off("click").on("click",
          function() {
            const index = $(this).closest("li").index()
            // Need to swap since songs are in reverse
            const displayIndex = self._getDisplayedIndex(index);
            if (self.current === displayIndex)
              $(self.cssSelector.jPlayer).jPlayer("play");
            else
              self.play(displayIndex);
            $(this).blur();
            return false;
          });

      // Create .live() handlers that disable free media links to force access via right click.
      $(`${playlistSelector} a.${this.options.playlistOptions.freeItemClass}`).off("click").on(
          "click", function() {
            $(this).closest("li").find("." + self.options.playlistOptions.itemClass).click();
            $(this).blur();
            return false;
          });

      function tooltip(selector, text) {
        $(`${playlistSelector} a.${selector}`).livequery(function() {
          $(this).attr("title", text)
        })
      }
      const options = this.options.playlistOptions
      tooltip(options.removeThisClass, "remove this item from the playlist")
      tooltip(options.removeUpClass, "remove this item and all items above it from the playlist")
      tooltip(options.removeDownClass, "remove this item and all items below it from the playlist")

      // Create .live() handlers for the remove controls
      $(playlistSelector).on("click", "a." + this.options.playlistOptions.removeItemClass, function() {
        const trigger = $(this)
        function removeItemAux(nextFunction, who) {
          // This has to be calculated before the removal, otherwise the who element is empty
          const next = nextFunction(who)
          self.remove(who.index(), function() {
            // if there is another next element to remove,
            // enqueue a removal after this current element is removed
            if (next.length > 0)
              removeItemAux(nextFunction, next)
          });
        }

        function getNextFunction() {
          if (trigger.hasClass(options.removeThisClass)) return _ => $()
          if (trigger.hasClass(options.removeUpClass)) return x => x.prev()
          assert(trigger.hasClass(options.removeDownClass))
          return x => x.next()
        }

        removeItemAux(getNextFunction(), trigger.closest("li"))
        return false;
      });
    },
    _updateControls: function() {
      const controls = $(`${this.cssSelector.playlist} .${this.options.playlistOptions.removeItemClass}`)
      if (this.options.playlistOptions.enableRemoveControls)
        controls.show()
      else
        controls.hide()
    },
    _highlight: function(index) {
      if (this.playlist.length && index !== undefined) {
        $(`${this.cssSelector.playlist} .jp-playlist-current`).removeClass("jp-playlist-current");
        $(`${this.cssSelector.playlist} li:nth-child(${index + 1})`).addClass("jp-playlist-current")
            .find(".jp-playlist-item").addClass("jp-playlist-current");
        $(`${this.cssSelector.title} li`).html(
            this.playlist[index].title
            + (this.playlist[index].artistName ? " <span class='jp-artist'>by "
                + this.playlist[index].artistName + "</span>" : ""));
      }
    },
    setPlaylist: async function(playlist, instant) {
      this._initPlaylist(playlist);
      await this._init(instant);
    },
    add: function(media, playNow) {
      const that = this
      if ($.isArray(media)) {
        media.forEach(x => that.add(x))
        return Promise.resolve()
      }
      if (this.playlist.some(e => e.file === media.file)) {
        console.log(`Entry ${media.file} already exists in playlist; skipping`)
        return Promise.resolve()
      }
      const playlistUl = $(this.cssSelector.playlist + " ul")
      playlistUl.prepend(this._createListItem(media)).find("li:first-child").hide()
          .slideDown(this.options.playlistOptions.addTime, function() {
            const regularHeightThreshold = 30
            const lastSong = playlistUl.find("li:first-child")
            if (lastSong.height() > regularHeightThreshold)
              console.log("too big, need to shorten")
          });
      this._updateControls();
      this.playlist.push(media);

      if (playNow)
        return this.play(this.playlist.length - 1);
      else if (this.playlist.length === 1)
        return this.select(0);
      else
        return Promise.resolve()
    },
    remove: async function(index, onEnd) {
      const self = this

      if (index === undefined) {
        this._initPlaylist([]);
        this._refresh(function() {
          $(self.cssSelector.jPlayer).jPlayer("clearMedia");
        });
        return true;
      }
      if (this.removing)
        return false;
      if (index < 0)
        return arguments.callee(self.playlist.length + index, onEnd)
      // index relates to end of array.
      if (index < this.playlist.length)
        this.removing = true;

      $(`${this.cssSelector.playlist} li:nth-child(${index + 1})`).slideUp(
          this.options.playlistOptions.removeTime,
          function() {
            $(this).remove();
            const playlistIndex = self._getDisplayedIndex(index)
            self.playlist.splice(playlistIndex, 1);
            if (self.playlist.length) {
              if (playlistIndex === self.current) {
                // Update current when last element was deleted.
                self.current = playlistIndex < self.playlist.length ? self.current : self.playlist.length - 1;
                self.select(self.current);
              } else if (playlistIndex < self.current)
                self.current--;
            } else {
              $(self.cssSelector.jPlayer).jPlayer("clearMedia");
              self.current = 0;
              self._updateControls();
            }

            self.removing = false;
            if (onEnd)
              onEnd()
          });
      return true;
    },
    select: function(index) {
      if (index < 0)
        return arguments.callee(this.playlist.length + index)
      // index relates to end of array.
      const displayIndex = this._getDisplayedIndex(index)
      if (index < this.playlist.length) {
        this.current = index;
        this._highlight(displayIndex);
        return Local.maybePreLoad(this.playlist[this.current])
            .then(s => $(this.cssSelector.jPlayer).jPlayer("setMedia", s));
      } else {
        this.current = 0;
        return new Promise(f => f())
      }
    },
    play: function(index) {
      if (index < 0)
        return arguments.callee(this.playlist.length + index)
      // index relates to end of array.
      if (index < this.playlist.length) {
        if (this.playlist.length) {
          return this.select(index).then(() => $(this.cssSelector.jPlayer).jPlayer("play"));
        }
      } else if (index === undefined) {
        $(this.cssSelector.jPlayer).jPlayer("play");
        return new Promise(f => f())
      }
    },
    pause: function() {
      $(this.cssSelector.jPlayer).jPlayer("pause");
    },
    next: function() {
      const index = (this.current + 1 < this.playlist.length) ? this.current + 1 : 0
      if (index > 0)
        this.play(index);
    },
    previous: function() {
      const index = (this.current - 1 >= 0) ? this.current - 1 : this.playlist.length - 1
      if (index < this.playlist.length - 1)
        this.play(index);
    },
    isLastSongPlaying: function() {
      return this.current === this.playlist.length - 1;
    },
    currentPlayingSong: function() {
      return this.playlist[this.current];
    }
  };
})(jQuery);
