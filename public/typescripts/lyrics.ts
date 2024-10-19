import {gplayer, gplaylist, Song} from "./types.js"

const HEBREW_REGEX = /[\u0590-\u05FF]/

class Helper {
  readonly lyricsDiv
  readonly lyricBox
  readonly lyricsContent
  readonly lyricsUrlBox

  previousContent = ""
  autoScroll = false
  scrollBaseline = 0
  timeBaseline = 0

  constructor() {
    this.lyricsDiv = $('#lyrics')
    // Wraps the actual lyrics with an extra div. Ensures the scroll bar is always on the right regardless of
    // text direction, and that there is a margin between the lyrics with the bar. Also ensures the bar is only
    // on the lyrics, not the buttons and text box.
    this.lyricBox = div({id: 'lyric-box'}).appendTo(this.lyricsDiv)
    this.lyricsContent = div({id: 'lyric-contents'}).appendTo(this.lyricBox)
    this.lyricsDiv.appendBr()
    const lyricsPusher = div().appendTo(this.lyricsDiv)
    this.lyricsUrlBox = $("<input id='lyrics-url' placeholder='Lyrics URL' type='text'/>").appendTo(lyricsPusher)
    const updateLyricsButton = button("Update lyrics").appendTo(lyricsPusher)
    const instrumentalSongButton = button("Instr. Song").appendTo(lyricsPusher)
    const instrumentalArtistButton = button("Instr. Artist").appendTo(lyricsPusher)
    validateBoxAndButton(
      this.lyricsUrlBox,
      updateLyricsButton,
      isValidUrl,
      this.updateLyrics.bind(this),
    )

    this.lyricsUrlBox.keyup(function () { // the validateBoxAndButton only applies to updateLyricsButton
      const box = $(this)

      function disable(b: JQuery<HTMLElement>): void {
        b.prop('disabled', box.val() !== "")
      }

      disable(instrumentalArtistButton)
      disable(instrumentalSongButton)
    })

    const that = this

    function setInstrumental(type: string): void {
      $.post(
        `lyrics/instrumental/${type}/${gplaylist.currentPlayingSong().file}`,
        s => that.showLyrics(s as string),
      )
    }

    instrumentalSongButton.click(() => setInstrumental("song"))
    instrumentalArtistButton.click(() => setInstrumental("artist"))


    const scrollableElement = this.lyricBox


    setInterval(this.scrollLyrics.bind(this), 100)

    scrollableElement.scroll(function () { // When the user scrolls manually, reset the baselines
      if (that.autoScroll.isFalse()) {
        that.scrollBaseline = scrollableElement.scrollTop()!
        that.timeBaseline = that.scrollBaseline && gplayer.percentageOfSongPlayed()
      }
      that.autoScroll = false
    })
  }
  updateLyrics(): void {
    const url = this.lyricsUrlBox.val() as string
    const songPath = gplaylist.currentPlayingSong().file
    $.ajax(
      "lyrics/push/" + songPath,
      {
        type: "POST",
        data: url,
        success: c => this.showLyrics(c),
        error: c => this.showLyrics(c.statusText),
        contentType: "text/plain",
      })
    this.clearButtons()
    this.lyricsContent.html("Pushing lyrics...")
  }
  scrollLyrics(): void {
    // Don't start scrolling right at the beginning of the song if there is no baseline set
    const heightBaseline = this.scrollBaseline || (this.lyricsContent.height()! / -4)
    const timePercentage = (gplayer.percentageOfSongPlayed() - this.timeBaseline) / 100.0
    this.autoScroll = true
    this.lyricBox.scrollTop(this.lyricBox.prop('scrollHeight') * timePercentage + heightBaseline)
  }
  showLyrics(content: string): void {
    this.clearButtons()
    this.autoScroll = true
    this.lyricsContent.html(content)
    const hasHebrew = content.search(HEBREW_REGEX) >= 0
    // The "direction" property also changes the overflow ruler; fuck that.
    this.lyricsContent.css("direction", hasHebrew ? "rtl" : "ltr")
    if (this.previousContent === this.lyricsContent.html()) // Ensure the same HTML formatting is used for comparison
      return // HTML wasn't changed, so don't reset the baselines
    this.previousContent = this.lyricsContent.html() // Ensure the same HTML formatting is used for comparison
    this.scrollBaseline = 0
    this.timeBaseline = 0
  }

  clearButtons(): void {
    this.lyricsUrlBox.val("")
    this.lyricsUrlBox.trigger("keyup")
  }
}

export class Lyrics {
  private static _helper: Helper | null
  private static get helper(): Helper {
    return this._helper || (this._helper = new Helper())
  }
  static show(song: Song): void {
    this.helper.clearButtons()
    this.helper.autoScroll = true
    this.helper.lyricsContent.html("Fetching lyrics...")
    const that = this
    $.get("lyrics/" + song.file, function (l) {
      that.helper.showLyrics(l)
      that.helper.scrollLyrics()
    })
  }
}
