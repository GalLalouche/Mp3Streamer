import {LastAlbum} from "./last.js"
import {Album, Artist, gplaylist, Song} from "./types.js"

export namespace Search {
  export async function quickSearch(): Promise<void> {
    const helper = getHelper()
    helper.clearResults()
    helper.searchBox.focus()
    return scan()
  }
}

const getHelper = lazy(() => new Helper())

const PLAY = "play"
const ADD = "plus"
const ADD_ENTIRE_ALBUM = "plus-square"
const ADD_DISC = "plus-circle"
const DOWNLOAD_FILE = "download"

interface Results {
  songs: Song[]
  albums: Album[]
  artists: Artist[]
}


class Helper {
  readonly searchBox = $("#searchbox")
  private readonly results = $("#search-results")
  private timeOfLastInput = 0

  constructor() {
    function getData(e: HTMLElement): any {return $(e).closest("li").data()}

    this.results.on("click", '#song-results .fa', function (e) {
      const song = getData(this)
      const isPlay = e.target.classList.contains("fa-play")
      $.get("data/song/" + song.file, e => gplaylist.add(e, isPlay))
    })
    this.results.on("click", `.album-result .fa-${ADD_ENTIRE_ALBUM}`, function (e) {
      if (e.target !== this) // Prevents clicks on anything other than that selector.
        return
      const album = getData(this)
      $.get("data/album/" + album.dir, e => gplaylist.add(e, false))
    })
    this.results.on("click", `.album-result .fa-${ADD_DISC}`, function () {
      const album = getData(this)
      const discNumber = $(this).closest("td").text()
      $.get(`data/disc/${discNumber}/${album.dir}`, e => gplaylist.add(e, false))
    })
    this.results.on("click", `.album-result .fa-${DOWNLOAD_FILE}`, function () {
      const album = getData(this)
      $.get("download/" + album.dir)
    })


    this.results.tabs()
    this.clearResults()
    this.searchBox
      .after(button("Scan+")
        .click(() => scanPlus())
        .attr("title", "Like Scan, but add the last new album to the playlist"))
      .after(button("Scan").click(() => scan()))
  }

  updateTimeOfLastInput(): number {
    const result = Date.now()
    this.timeOfLastInput = result
    return result
  }

  private getTime(): number {
    return parseInt(this.results.attr("time")!)
  }

  private setTime(n: number): void {
    this.results.attr("time", n.toString())
  }

  setResults(results: Results, requestTime: number): void {
    this.results.show()

    function specificResults<A>(
      key: keyof Results,
      itemProducer: (a: A) => string,
      appendTo?: JQuery<HTMLElement>,
      array?: any[],
    ): void {
      const name = key.substring(0, key.length - 1)
      const ul = elem("ul").appendTo(appendTo || $(`#${name}-results`).empty())
      $.each(array || results[key], function (_, e) {
        const li = $(`<li class="${name}-result search-result">${itemProducer(e)}</li>`)
        li.appendTo(ul).data(e)
        li.attr("title", "")
        li.mouseover(function () {
          if (!li.attr('title') && li.custom_overflown())
            li.custom_tooltip(`${itemProducer(e).split(">").custom_last().trim()}`)
        })
      })
    }

    if (this.getTime() > requestTime)
      return // A later request has already set the result.
    this.setTime(requestTime)

    specificResults("songs", function (song: Song) {
        function suffix() {
          if (!song.composer) // Assumes all classical pieces have a composer field.
            return `${song.artistName}: ${song.title} (${song.duration.timeFormat()})`

          // TODO handle code duplication of all the toStrings for composers
          const albumNameContainsComposer = song.albumName.toLowerCase().includes(song.composer.toLowerCase())
          const base = `${song.artistName}: ${song.title}`
          const pieceTitle = albumNameContainsComposer ? song.albumName : `${song.composer}'s ${song.albumName}`
          const opus = song.opus ? `, ${song.opus}` : ''
          return `${base}, ${pieceTitle}${opus}`
        }

        return `${icon(ADD)} ${icon(PLAY)} ${suffix()}`
      },
    )
    $.each($(".song-result"), function () {
      const song = $(this).data() as Song
      $(this).custom_tooltip(gplaylist.toString(song))
    })

    specificResults("albums", function (album: Album) {
      function albumText() {
        if (!album.composer) // Assumes all classical pieces have a composer field.
          return `${album.artistName}: ${album.year || "NO_YEAR"} ${album.title}`

        const titleContainsComposer = album.title.toLowerCase().includes(album.composer.toLowerCase())
        const pieceTitle = titleContainsComposer ? album.title : `${album.composer}'s ${album.title}`
        const base = `${album.artistName}: ${pieceTitle}`
        const opus = album.opus ? `, ${album.opus}` : ''
        const other = [album.performanceYear, album.conductor, album.orchestra].filter(x => x)
        return base + opus + (other ? ` (${other.join(", ")})` : '')
      }

      const item = `${icon(ADD_ENTIRE_ALBUM)} ${icon(DOWNLOAD_FILE)} ` + albumText()
      if (album.discNumbers) {
        const discNumberElements = album.discNumbers.map(d => `<td>${icon(ADD_DISC)}${d}</td>`).join(" ")
        // Using a table handles the case where there are too many disc numbers to fit a in single line, e.g.,
        // https://en.wikipedia.org/wiki/The_Color_Spectrum. In such a case, the list will simply overflow to
        // the next line.
        return item + `<table id="discNumbers"><tr>${discNumberElements}</tr></table>`
      } else
        return item
    })

    specificResults("artists", () => "")
    $.each($(".artist-result"), function () {
      const li = $(this)
      const artist = li.data()
      const albums = div().appendTo(li)
      specificResults("albums", (a: Album) => `${icon(ADD)} ${a.year} ${a.title}`, albums, artist.albums)
      li.accordion({
        collapsible: true,
        active: false,
        heightStyle: "content",
      })
    })
  }

  clearResults(): void {
    this.searchBox.val('')
    this.results.hide()
    // TODO Can we change attr automatically? Do we want to?
    this.setTime(this.updateTimeOfLastInput()) // Ensure late results will be ignored.
  }

  static create(): Helper {
    const result = getHelper()
    result.updateTimeOfLastInput()
    // Blur search box after enough time has passed and it wasn't updated. By blurring the box,
    // keyboard shortcuts are Re-enabled. This way, after 10 minutes of playing, you can still press
    // 'K' to pause the damn thing.
    const INPUT_TIMEOUT_IN_MILLIS = 10000
    setInterval(function () {
      if (Date.now() - result.timeOfLastInput > INPUT_TIMEOUT_IN_MILLIS)
        result.searchBox.blur()
    }, INPUT_TIMEOUT_IN_MILLIS)

    function search() {
      const searchTime = result.updateTimeOfLastInput()
      const text = result.searchBox.val()
      if (text === "") {
        result.clearResults()
        return
      }
      $.get("search/" + text, e => result.setResults(e, searchTime))
    }

    result.searchBox.bind('input change', search)

    // When Enter is pressed and there is a *single* search result in the active tab, click it.
    result.searchBox.on("keydown", function (e) {
      if (e.keyCode !== ENTER_CODE)
        return
      const activeTab = "[role=tabpanel]:not([style*='display: none'])"
      const items = result.results.find(`${activeTab} ul li`)
      if (items.length !== 1)
        return
      // Click the first icon on the item list.
      items.first().find("i").first().click()
      result.clearResults()
    })


    return result
  }
}

async function scanAux(addNewAlbum: boolean): Promise<void> {
  await (addNewAlbum ? LastAlbum.addNextNewAlbum() : LastAlbum.updateLatestAlbum())
  return $.get("index/index").toPromise()
}

async function scan(): Promise<void> {
  return scanAux(false)
}

export async function scanPlus() {
  return scanAux(true)
}

$(() => {
  Helper.create()
})
