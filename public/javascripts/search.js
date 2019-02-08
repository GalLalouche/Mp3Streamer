$(function() {
  const PLAY = "play"
  const ADD = "plus"
  const ADD_ENTIRE_ALBUM = "plus-square"
  const ADD_DISC = "plus-circle"
  const DOWNLOAD_FILE = "download"

  const searchBox = $("#searchbox");
  const results = $("#search-results")

  function setResults(jsArray, requestTime) {
    results.show()

    function specificResults(name, itemProducer, appendTo, array) {
      const ul = elem("ul").appendTo(appendTo || $(`#${name}-results`).empty())
      $.each(array || jsArray[`${name}s`], function(_, e) {
        const li = $(`<li class="${name}-result search-result">${itemProducer(e)}</li>`)
        li.appendTo(ul).data(e)
        li.attr("title", "")
        li.mouseover(function() {
          if (li.custom_overflown())
            li.custom_tooltip(`${itemProducer(e).split(">").custom_last().trim()}`)
        })
      })
    }

    if (parseInt(results.attr("time")) > requestTime)
      return // A later request has already set the result.
    results.attr("time", requestTime)

    specificResults("song", function(song) {
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
    })
    $.each($(".song-result"), function() {
      const song = $(this).data()
      $(this).custom_tooltip(`${song.year}, ${song.albumName}, ${song.track}`)
    })

    specificResults("album", function(album) {
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
        const discNumberElements = album.discNumbers.map(d => `<span>${icon(ADD_DISC)}${d}</span>`).join(" ")
        return item + "<br>&emsp;&emsp;" + discNumberElements
      } else
        return item
    })

    specificResults("artist", () => "")
    $.each($(".artist-result"), function() {
      const li = $(this)
      const artist = li.data()
      const albums = div().appendTo(li)
      specificResults("album", a => `${icon(ADD)} ${a.year} ${a.title}`, albums, artist.albums)
      li.accordion({
        collapsible: true,
        active: false,
        heightStyle: "content",
      })
    })
  }

  function clearResults() {
    searchBox.val('')
    results.hide()
    // TODO Can we change attr automatically? Do we want to?
    results.attr("time", updateTimeOfLastInput()) // Ensure late results will be ignored.
  }

  function scan() {
    LastAlbum.reopenLastAlbumWebsocketIfNeeded()
    $.get("debug/fast_refresh", function() {
      openConnection("refresh", function(msg, connection) {
        try {
          $.toast("Found new directory: " + JSON.parse(msg.data).currentDir)
        } catch (ignored) {
          if (msg.data.includes("Finished")) {
            connection.close()
            search()
          }
        }
      })
    })
  }

  results.on("click", '#song-results .fa', function(e) {
    const song = $(this).parent().data()
    const isPlay = e.target.classList.contains("fa-play")
    $.get("data/songs/" + song.file, e => gplaylist.add(e, isPlay))
  })
  results.on("click", `.album-result .fa-${ADD_ENTIRE_ALBUM}`, function() {
    const album = $(this).parent().data()
    $.get("data/albums/" + album.dir, e => gplaylist.add(e, false))
  })
  results.on("click", `.album-result .fa-${ADD_DISC}`, function() {
    const album = $(this).parent().parent().data()
    const discNumber = $(this).parent().text()
    $.get(`data/discs/${discNumber}/${album.dir}`, e => gplaylist.add(e, false))
  })
  results.on("click", `.album-result .fa-${DOWNLOAD_FILE}`, function() {
    const album = $(this).parent().data()
    $.get("download/" + album.dir)
  })

  let timeOfLastInput = 0

  function updateTimeOfLastInput() {
    timeOfLastInput = Date.now()
    return timeOfLastInput
  }

  searchBox.bind('input change', search)
  function search() {
    const searchTime = updateTimeOfLastInput()
    const text = searchBox.val()
    if (text === "") {
      clearResults()
      return
    }
    $.get("search/" + text, e => setResults(e, searchTime))
  }

  results.tabs()
  clearResults()
  searchBox.after(button("Scan").click(function() {
    searchBox.focus()
    scan()
  }))

  // Blur search box after enough time has passed and it wasn't updated. By blurring the box, keyboard
  // shortcuts are Re-enabled. This way, after 10 minutes of playing, you can still press 'K' to pause the
  // damn thing.
  const INPUT_TIMEOUT_IN_MILLIS = 10000
  setInterval(function() {
    if (Date.now() - timeOfLastInput > INPUT_TIMEOUT_IN_MILLIS)
      searchBox.blur()
  }, INPUT_TIMEOUT_IN_MILLIS)
  Search.quickSearch = function() {
    clearResults()
    searchBox.focus()
    scan()
  }
})

Search = {}
