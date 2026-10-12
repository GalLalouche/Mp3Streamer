import * as API from "./api.js"
import {Startup} from "./events.js"
import {Song} from "./media.js"
import {gplayer, gplaylist} from "./player_singleton.js"

Startup.promise.then(ps =>
  ps.player.listen(pe => {
    if (pe instanceof Song)
      show(pe)
  })
)

function show(song: Song): void {
  const helper = getHelper()
  helper.clearButtons()
  helper.autoScroll = true
  helper.lyricsContent.html("Fetching lyrics...")
  getLyrics(song).then(l => {
    helper.showLyrics(l)
    helper.scrollLyrics()
  })
}

const getHelper = lazy(() => Helper.create())

async function getLyrics(song: Song): Promise<string> {
  return API.get("lyrics/" + song.file).then(e => e as unknown as string)
}

const HEBREW_REGEX = /[\u0590-\u05FF]/

class Helper {
  readonly lyricsDiv: JQuery<HTMLElement>
  readonly lyricBox: JQuery<HTMLElement>
  readonly lyricsContent: JQuery<HTMLElement>
  readonly lyricsUrlBox: JQuery<HTMLElement>

  previousContent = ""
  autoScroll = false
  scrollBaseline = 0
  timeBaseline = 0

  constructor(
    lyricsDiv: JQuery<HTMLElement>,
    lyricBox: JQuery<HTMLElement>,
    lyricsContent: JQuery<HTMLElement>,
    lyricsUrlBox: JQuery<HTMLElement>,
  ) {
    this.lyricsDiv = lyricsDiv
    this.lyricBox = lyricBox
    this.lyricsContent = lyricsContent
    this.lyricsUrlBox = lyricsUrlBox
  }

  static create(): Helper {
    const lyricsDiv = $('#lyrics')
    // Wraps the actual lyrics with an extra div. Ensures the scroll bar is always on the right regardless of
    // text direction, and that there is a margin between the lyrics with the bar. Also ensures the bar is only
    // on the lyrics, not the buttons and text box.
    const lyricBox = div({id: 'lyric-box'}).appendTo(lyricsDiv)
    const lyricsContent = div({id: 'lyric-contents'}).appendTo(lyricBox)
    lyricsDiv.appendBr()
    const lyricsPusher = div().appendTo(lyricsDiv)
    const lyricsUrlBox = $("<input id='lyrics-url' placeholder='Lyrics URL' type='text'/>").appendTo(lyricsPusher)
    const updateLyricsButton = button("Update lyrics").appendTo(lyricsPusher)

    const result = new Helper(lyricsDiv, lyricBox, lyricsContent, lyricsUrlBox)

    validateBoxAndButton(
      lyricsUrlBox,
      updateLyricsButton,
      isValidUrl,
      () => result.updateLyrics(),
    )

    const instrumentalSongButton = button("Instr. Song").appendTo(lyricsPusher)
    const instrumentalArtistButton = button("Instr. Artist").appendTo(lyricsPusher)
    lyricsUrlBox.on('keyup', function () { // the validateBoxAndButton only applies to updateLyricsButton
      const box = $(this)

      function disable(b: JQuery<HTMLElement>): void {
        b.prop('disabled', box.val() !== "")
      }

      disable(instrumentalArtistButton)
      disable(instrumentalSongButton)
    })

    function setInstrumental(type: string): void {
      $.post(
        `lyrics/instrumental/${type}/${gplaylist.currentPlayingSong().file}`,
        s => result.showLyrics(s as string),
      )
    }

    instrumentalSongButton.on('click', () => setInstrumental("song"))
    instrumentalArtistButton.on('click', () => setInstrumental("artist"))


    const scrollableElement = result.lyricBox


    setInterval(() => result.scrollLyrics(), 100)

    scrollableElement.on('scroll', function () { // When the user scrolls manually, reset the baselines
      if (result.autoScroll.isFalse()) {
        result.scrollBaseline = scrollableElement.scrollTop()!
        result.timeBaseline = result.scrollBaseline && gplayer.percentageOfSongPlayed().zeroToHundred()
      }
      result.autoScroll = false
    })
    return result
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
    const timePercentage =
      (gplayer.percentageOfSongPlayed().zeroToHundred() - this.timeBaseline) / 100.0
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
