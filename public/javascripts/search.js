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

  results.on("click", "#song-results .fa", function(e) {
    const song = $(this).parent().data()
    const isPlay = e.target.classList.contains("fa-play")
    $.get("data/songs/" + song.file, e => gplaylist.add(e, isPlay))
  })
  results.on("click", ".album-result .fa", function() {
    const album = $(this).parent().data()
    $.get("data/albums/" + album.dir, e => gplaylist.add(e, false))
  })

  $("#searchbox").bind('input change', function () {
    const text = $(this).val()
    if (text === "") {
      return $("#clear-results").click()
    }
    $.get("search/" + text, e => setResults(Date.now(), e))
  })
  results.tabs()
  $("#clear-results").click()
})
