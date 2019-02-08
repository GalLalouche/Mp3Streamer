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

  jPlayerPlaylist = function(cssSelector, playlist, options) {
    const self = this

    this.current = 0;
    this.loop = false; // Flag used with the jPlayer repeat event
    this.shuffled = false;
    this.removing = false; // Flag is true during remove animation,
    // disabling the remove() method until complete.

    this.cssSelector = $.extend({}, this._cssSelector, cssSelector); // Object:
    // Containing the css selectors for jPlayer and its cssSelectorAncestor
    this.options = $.extend(true, {}, this._options, options); // Object:
    // The jPlayer constructor/ options for this playlist and the playlist options

    this.playlist = []; // Array of Objects: The current playlist displayed (Un-shuffled or Shuffled)
    this.original = []; // Array of Objects: The original playlist

    this._initPlaylist(playlist); // Copies playlist to this.original.
    // Then mirrors this.original to this.playlist. Creating two arrays, where the element pointers match.
    // (Enables pointer comparison.)

    // Setup the css selectors for the extra interface items used by the playlist.
    this.cssSelector.title = this.cssSelector.cssSelectorAncestor + " .jp-title"; // Note
    // that the text is written to the decendant li node.
    this.cssSelector.playlist = this.cssSelector.cssSelectorAncestor + " .jp-playlist";
    this.cssSelector.next = this.cssSelector.cssSelectorAncestor + " .jp-next";
    this.cssSelector.previous = this.cssSelector.cssSelectorAncestor + " .jp-previous";
    this.cssSelector.shuffle = this.cssSelector.cssSelectorAncestor + " .jp-shuffle";
    this.cssSelector.shuffleOff = this.cssSelector.cssSelectorAncestor + " .jp-shuffle-off";

    // Override the cssSelectorAncestor given in options
    this.options.cssSelectorAncestor = this.cssSelector.cssSelectorAncestor;

    // Override the default repeat event handler
    this.options.repeat = function(event) {
      self.loop = event.jPlayer.options.loop;
    };

    // Create a ready event handler to initialize the playlist
    $(this.cssSelector.jPlayer).bind($.jPlayer.event.ready, function(event) {
      self._init();
    });

    // Create an ended event handler to move to the next item
    $(this.cssSelector.jPlayer).bind($.jPlayer.event.ended, function(event) {
      self.next();
    });

    // Create a play event handler to pause other instances
    $(this.cssSelector.jPlayer).bind($.jPlayer.event.play, function(event) {
      $(this).jPlayer("pauseOthers");
    });

    // Create a resize event handler to show the title in full screen mode.
    $(this.cssSelector.jPlayer).bind($.jPlayer.event.resize, function(event) {
      if (event.jPlayer.options.fullScreen) {
        $(self.cssSelector.title).show();
      } else {
        $(self.cssSelector.title).hide();
      }
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

    $(this.cssSelector.shuffle).click(function() {
      self.shuffle(true);
      return false;
    });
    $(this.cssSelector.shuffleOff).click(function() {
      self.shuffle(false);
      return false;
    }).hide();

    // Put the title in its initial display state
    if (!this.options.fullScreen) {
      $(this.cssSelector.title).hide();
    }

    // Remove the empty <li> from the page HTML. Allows page to be valid HTML, while not interfereing with display animations
    $(this.cssSelector.playlist + " ul").empty();

    // Create .live() handlers for the playlist items along with the free/ media and remove controls.
    this._createItemHandlers();

    // Instance jPlayer
    $(this.cssSelector.jPlayer).jPlayer(this.options);
  };

  jPlayerPlaylist.prototype = {
    _cssSelector: { // static object, instanced in constructor
      jPlayer: "#jquery_jplayer_1",
      cssSelectorAncestor: "#jp_container_1"
    },
    _options: { // static object, instanced in constructor
      playlistOptions: {
        autoPlay: true,
        loopOnPrevious: false,
        shuffleOnLoop: true,
        enableRemoveControls: true,
        displayTime: 'slow',
        addTime: 'fast',
        removeTime: 'fast',
        shuffleTime: 'slow',
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
    option: function(option, value) { // For changing playlist options
      // only
      if (value === undefined) {
        return this.options.playlistOptions[option];
      }

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
          if (self.options.playlistOptions.autoPlay) {
            self.play(self.current);
          } else {
            self.select(self.current);
          }
        });
      }
    },
    _initPlaylist: function(playlist) {
      this.current = 0;
      this.shuffled = true;
      this.removing = false;
      this.original = $.extend(true, [], playlist); // Copy the Array of
      // Objects
      this._originalPlaylist();
    },
    _originalPlaylist: function() {
      this.playlist = [];
      // Make both arrays point to the same object elements. Gives us 2
      // different arrays, each pointing to the same actual object. ie.,
      // Not copies of the object.
      for (let i = 0; i < this.original.length; i++)
        this.playlist[i] = this.original[this.original.length - i - 1];
    },
    _refresh: function(instant) {
      /*
       * instant: Can be undefined, true or a function. undefined -> use
       * animation timings true -> no animation function -> use animation
       * timings and excute function at half way point.
       */
      const self = this

      if (instant && !$.isFunction(instant)) {
        $(this.cssSelector.playlist + " ul").empty();
        $.each(this.playlist, function(i, v) {
          $(self.cssSelector.playlist + " ul").append(self._createListItem(self.playlist[i]));
        });
        this._updateControls();
      } else {
        const displayTime = $(this.cssSelector.playlist + " ul").children().length ? this.options.playlistOptions.displayTime
            : 0

        $(this.cssSelector.playlist + " ul").slideUp(displayTime, function() {
          const $this = $(this)
          $(this).empty();

          $.each(self.playlist, function(i, v) {
            $this.append(self._createListItem(self.playlist[i]));
          });
          self._updateControls();
          if ($.isFunction(instant)) {
            instant();
          }
          if (self.playlist.length) {
            $(this).slideDown(self.options.playlistOptions.displayTime);
          } else {
            $(this).show();
          }
        });
      }
    },
    _createListItem: function(media) {
      const self = this;

      // Wrap the <li> contents in a <div>
      let listItem = "<li><div>"

      const options = this.options.playlistOptions
      // Create remove controls
      function appendRemoveItem(clazz, char) {
        listItem += `<a href='javascript:;' class='${options.removeItemClass} ${clazz}'>${char}</a>`;
      }

      // Create links to free media
      if (media.free) {
        let first = true;
        listItem += "<span class='" + this.options.playlistOptions.freeGroupClass + "'>(";
        $.each(media, function(property, value) {
          if ($.jPlayer.prototype.format[property]) { // Check
            // property is a media format.
            if (first) {
              first = false;
            } else {
              listItem += " | ";
            }
            listItem += "<a class='" + self.options.playlistOptions.freeItemClass + "' href='" + value
                + "' tabindex='1'>" + property + "</a>";
          }
        });
        listItem += ")</span>";
      }

      // The title is given next in the HTML otherwise the float:right on the free media corrupts in IE6/7
      const itemHref = `<a href='javascript:;' class='${this.options.playlistOptions.itemClass}' tabindex='1'>`
          + `${media.title} <span class='jp-artist'>${playlistUtils.mediaMetadata(media)}</span>`
          + `</a>`
      listItem += `<span class='playlist-item'>${itemHref}</span>`
      appendRemoveItem(options.removeThisClass, "&times;")
      appendRemoveItem(options.removeUpClass, "&uparrow;")
      appendRemoveItem(options.removeDownClass, "&downarrow;")
      listItem += "</div></li>";

      return listItem;
    },
    _createItemHandlers: function() {
      const self = this;
      // Create .live() handlers for the playlist items
      $(this.cssSelector.playlist + " a." + this.options.playlistOptions.itemClass).off("click").on("click",
          function() {
            const index = $(this).closest("li").index()
            // Need to swap since songs are in reverse
            const displayIndex = self._getDisplayedIndex(index);
            if (self.current !== displayIndex) {
              self.play(displayIndex);
            } else {
              $(self.cssSelector.jPlayer).jPlayer("play");
            }
            $(this).blur();
            return false;
          });

      // Create .live() handlers that disable free media links to force access via right click
      $(self.cssSelector.playlist + " a." + this.options.playlistOptions.freeItemClass).off("click").on(
          "click", function() {
            $(this).parent().parent().find("." + self.options.playlistOptions.itemClass).click();
            $(this).blur();
            return false;
          });

      function tooltip(selector, text) {
        $(self.cssSelector.playlist + " a." + selector).livequery(function() {
          $(this).attr("title", text)
        })
      }
      const options = this.options.playlistOptions
      tooltip(options.removeThisClass, "remove this item from the playlist")
      tooltip(options.removeUpClass, "remove this item and all items above it from the playlist")
      tooltip(options.removeDownClass, "remove this item and all items below it from the playlist")

      // Create .live() handlers for the remove controls
      $(self.cssSelector.playlist).on("click", "a." + this.options.playlistOptions.removeItemClass, function() {
        const triggerer = $(this)
        function removeItemAux(nextFunction, who) {
          // This has to be calculated before the removal, otherwise the who element is empty
          const next = nextFunction(who)
          self.remove(who.index(), function() {
            // if there is another next element to remove, enqueue a removal after this current element is removed
            if (next.length > 0)
              removeItemAux(nextFunction, next)
          });
        }

        function getNextFunction() {
          if (triggerer.hasClass(options.removeThisClass)) return _ => $()
          if (triggerer.hasClass(options.removeUpClass)) return x => x.prev()
          if (triggerer.hasClass(options.removeDownClass)) return x => x.next()
        }

        removeItemAux(getNextFunction(), triggerer.parent().parent())
        return false;
      });
    },
    _updateControls: function() {
      if (this.options.playlistOptions.enableRemoveControls) {
        $(this.cssSelector.playlist + " ." + this.options.playlistOptions.removeItemClass).show();
      } else {
        $(this.cssSelector.playlist + " ." + this.options.playlistOptions.removeItemClass).hide();
      }
      if (this.shuffled) {
        $(this.cssSelector.shuffleOff).show();
        $(this.cssSelector.shuffle).hide();
      } else {
        $(this.cssSelector.shuffleOff).hide();
        $(this.cssSelector.shuffle).show();
      }
    },
    _highlight: function(index) {
      if (this.playlist.length && index !== undefined) {
        $(this.cssSelector.playlist + " .jp-playlist-current").removeClass("jp-playlist-current");
        $(this.cssSelector.playlist + " li:nth-child(" + (index + 1) + ")").addClass("jp-playlist-current")
            .find(".jp-playlist-item").addClass("jp-playlist-current");
        $(this.cssSelector.title + " li").html(
            this.playlist[index].title
            + (this.playlist[index].artistName ? " <span class='jp-artist'>by "
            + this.playlist[index].artistName + "</span>" : ""));
      }
    },
    setPlaylist: function(playlist, instant) {
      playlist = playlist
      this._initPlaylist(playlist);
      this._init(instant);
    },
    add: function(media, playNow) {
      const that = this
      if ($.isArray(media)) {
        $.each(media, function(i, track) {
          that.add(track);
        });
        return;
      }
      if (this.playlist.some(e => e.file == media.file)) {
        console.log(`Entry ${media.file} already exists in playlist; skipping`)
        return
      }
      // GAL - changed here to add to beginning of list
      const playlistUl = $(this.cssSelector.playlist + " ul")
      playlistUl.prepend(this._createListItem(media)).find("li:first-child").hide()
          .slideDown(this.options.playlistOptions.addTime, function() {
            const regularHeightThreshold = 30
            const lastSong = playlistUl.find("li:first-child")
            if (lastSong.height() > regularHeightThreshold)
              console.log("too big, need to shorten")
          });
      const foo = playlistUl.find("li:first-child")
      this._updateControls();
      this.original.push(media);
      // Both array elements share the same object pointer. Comforms with _initPlaylist(p) system.
      this.playlist.push(media);

      if (playNow) {
        this.play(this.playlist.length - 1);
      } else {
        if (this.original.length === 1) {
          this.select(0);
        }
      }
    },
    remove: function(index, onEnd) {
      const self = this

      if (index === undefined) {
        this._initPlaylist([]);
        this._refresh(function() {
          $(self.cssSelector.jPlayer).jPlayer("clearMedia");
        });
        return true;
      } else {

        if (this.removing) {
          return false;
        } else {
          index = (index < 0) ? self.original.length + index : index; // Negative
          // index relates to end of array.
          if (0 <= index && index < this.playlist.length) {
            this.removing = true;

            $(this.cssSelector.playlist + " li:nth-child(" + (index + 1) + ")").slideUp(
                this.options.playlistOptions.removeTime,
                function() {
                  $(this).remove();

                  if (self.shuffled) {
                    index = self.playlist.length - index - 1
                    const item = self.playlist[index]
                    $.each(self.original, function(i, v) {
                      if (self.original[i] === item) {
                        self.original.splice(i, 1);
                        return false; // Exit $.each
                      }
                    });
                    self.playlist.splice(index, 1);
                  } else {
                    self.original.splice(index, 1);
                    self.playlist.splice(index, 1);
                  }

                  if (self.original.length) {
                    if (index === self.current) {
                      self.current = (index < self.original.length) ? self.current
                          : self.original.length - 1; // To
                      // cope when last element being selected when it was removed
                      self.select(self.current);
                    } else if (index < self.current) {
                      self.current--;
                    }
                  } else {
                    $(self.cssSelector.jPlayer).jPlayer("clearMedia");
                    self.current = 0;
                    self.shuffled = false;
                    self._updateControls();
                  }

                  self.removing = false;
                  if (onEnd)
                    onEnd()
                });
          }
          return true;
        }
      }
    },
    select: function(index) {
      index = (index < 0) ? this.original.length + index : index; // Negative
      // index relates to end of array.
      const displayIndex = this._getDisplayedIndex(index)
      if (0 <= index && index < this.playlist.length) {
        this.current = index;
        this._highlight(displayIndex);
        $(this.cssSelector.jPlayer).jPlayer("setMedia", this.playlist[this.current]);
      } else {
        this.current = 0;
      }
    },
    play: function(index) {
      index = (index < 0) ? this.original.length + index : index; // Negative
      // index relates to end of array.
      if (0 <= index && index < this.playlist.length) {
        if (this.playlist.length) {
          this.select(index);
          $(this.cssSelector.jPlayer).jPlayer("play");
        }
      } else if (index === undefined) {
        $(this.cssSelector.jPlayer).jPlayer("play");
      }
    },
    pause: function() {
      $(this.cssSelector.jPlayer).jPlayer("pause");
    },
    next: function() {
      const index = (this.current + 1 < this.playlist.length) ? this.current + 1 : 0
      if (this.loop) {
        // See if we need to shuffle before looping to start, and only shuffle if more than 1 item.
        if (index === 0 && this.shuffled && this.options.playlistOptions.shuffleOnLoop
            && this.playlist.length > 1) {
          this.shuffle(true, true); // playNow
        } else {
          this.play(index);
        }
      } else {
        // The index will be zero if it just looped round
        if (index > 0) {
          this.play(index);
        }
      }
    },
    previous: function() {
      const index = (this.current - 1 >= 0) ? this.current - 1 : this.playlist.length - 1

      if (this.loop && this.options.playlistOptions.loopOnPrevious || index < this.playlist.length - 1) {
        this.play(index);
      }
    },
    shuffle: function(shuffled, playNow) {
      const self = this

      if (shuffled === undefined) {
        shuffled = !this.shuffled;
      }

      if (shuffled || shuffled !== this.shuffled) {

        $(this.cssSelector.playlist + " ul").slideUp(this.options.playlistOptions.shuffleTime, function() {
          self.shuffled = shuffled;
          if (shuffled) {
            self.playlist.sort(function() {
              return 0.5 - Math.random();
            });
          } else {
            self._originalPlaylist();
          }
          self._refresh(true); // Instant

          if (playNow || !$(self.cssSelector.jPlayer).data("jPlayer").status.paused) {
            self.play(0);
          } else {
            self.select(0);
          }

          $(this).slideDown(self.options.playlistOptions.shuffleTime);
        });
      }
    },
    isLastSongPlaying: function() {
      return this.current === this.playlist.length - 1;
    },
    currentPlayingSong: function() {
      return this.playlist[this.current];
    }
  };
})(jQuery);
