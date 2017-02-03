$(function() {
  const results = $("#search-results")
  function setResults(requestTime, jsArray) {
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
    specificResults("song",  s => 
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
      const albums = elem("div").appendTo(li)
      specificResults("album", a => `${addIcon} ${a.year} ${a.title}`, albums, artist.albums)
      li.accordion({
        collapsible: true,
        active: false,
        heightStyle: "content",
      })
    })
  }

  function clearResults() {
    $('#search-results').hide()
    $('#searchbox').val('')
  }
  function scan() {
    $.get("debug/fast_refresh", function() {
      openConnection("refresh", function(msg) {
        try {
          $.toast("Found new directory: " + JSON.parse(msg.data).currentDir)
        } catch (e) {
          $.toast(msg.data)
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
  const inputTimeout = 10000
  $("#searchbox").bind('input change', function () {
    timeOfLastInput = Date.now()
    const text = $(this).val()
    if (text === "")
      clearResults()
    $.get("search/" + text, e => setResults(Date.now(), e))
  })
  results.tabs()
  clearResults()
  const scanButton = elem("button", "Scan")
  scanButton.click(function() {
    scan()
  })
  $("#searchbox").after(scanButton)

  // Blur search box after enough time has passed and it wasn't updated. By blurring the box, keyboard shortcuts are
  // Re-enabled. This way, after 10 minutes of playing, you can still press 'K' to pause the damn thing.
  setInterval(function() {
    if (Date.now() - timeOfLastInput > inputTimeout)
      $("#searchbox").blur()
  }, inputTimeout)
})
