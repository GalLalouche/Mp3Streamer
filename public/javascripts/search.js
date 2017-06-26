$(function() {
  const searchBox = $("#searchbox");
  const results = $("#search-results")

  function setResults(jsArray, requestTime) {
    results.show()
    const icon = name => `<i class="fa fa-${name}"/>`
    const playIcon = icon("play")
    const addIcon = icon("plus")

    function specificResults(name, itemProducer, appendTo, array) {
      const ul = elem("ul").appendTo(appendTo || $(`#${name}-results`).empty())
      $.each(array || jsArray[`${name}s`], function(_, e) {
        $(`<li class="${name}-result">${itemProducer(e)}</li>`)
            .appendTo(ul)
            .data(e)
      })
    }

    if (parseInt(results.attr("time")) > requestTime)
      return // a later request has already set the result
    results.attr("time", requestTime)
    specificResults("song", s =>
        `${addIcon} ${playIcon} ${s.artistName}: ${s.title} (${s.duration.timeFormat()})`)
    $.each($(".song-result"), function() {
      const song = $(this).data()
      $(this).attr("title", `${song.year}, ${song.albumName}, ${song.track}`)
    })
    const albumItem = a => `${playIcon} ${a.artistName}: ${a.title} (${a.year || "N/A"})`
    specificResults("album", albumItem)
    specificResults("artist", _ => "")
    $.each($(".artist-result"), function() {
      const li = $(this)
      const artist = li.data()
      const albums = div().appendTo(li)
      specificResults("album", a => `${addIcon} ${a.year} ${a.title}`, albums, artist.albums)
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
    // TODO can we change attr automatically? Do we want to?
    results.attr("time", updateTimeOfLastInput()) // ensure late results will be ignored
  }

  function scan() {
    $.get("debug/fast_refresh", function() {
      openConnection("refresh", function(msg, connection) {
        try {
          $.toast("Found new directory: " + JSON.parse(msg.data).currentDir)
        } catch (e) {
          $.toast(msg.data)
          if (msg.data.includes("Finished")) {
            connection.close()
            search()
          }
        }
      })
    })
  }

  results.on("click", "#song-results .fa", function(e) {
    const song = $(this).parent().data()
    const isPlay = e.target.classList.contains("fa-play")
    $.get("data/songs/" + song.file, e => gplaylist.add(e, isPlay))
  })
  results.on("click", ".album-result .fa", function() {
    const album = $(this).parent().data()
    $.get("data/albums/" + album.dir, e => gplaylist.add(e, false))
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

  // Blur search box after enough time has passed and it wasn't updated. By blurring the box, keyboard shortcuts are
  // Re-enabled. This way, after 10 minutes of playing, you can still press 'K' to pause the damn thing.
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
