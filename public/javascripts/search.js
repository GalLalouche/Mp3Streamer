$(function() {
  function setResults(requestTime, jsArray) {
    $("#search-results").show()
    const icon = name => `<i class="fa fa-${name}"/>`
    function specificResults(name, itemProducer) {
      const tab = $(`#${name}-results`)
      tab.empty()
      tab.append('<ul/>')
      const ul = tab.find('ul')
      $.each(jsArray[`${name}s`], function(_, s) {
        const item = itemProducer(s)
        const li = $(`<li class="${name}-result">${item}</li>`)
        li.appendTo(ul)
        li.data(s)
      })
    }
    const results = $("#search-results")
    if (results.attr("time") > requestTime)
      return // a later request has already set the result
    results.attr("time", requestTime)
    specificResults("song",  s => `${icon("play")} ${icon("plus")} ${s.artistName}: ${s.title} (${s.duration.timeFormat()})`)
    $.each($(".song-result"), function(_, e) {
      const song = $(this).data()
      $(this).attr("title", `${song.year}, ${song.albumName}, ${song.track}`)
    })
    specificResults("album", a => `${icon("plus")} ${a.artistName}: ${a.title} (${a.year | "N/A"})`)
    specificResults("artist", a => a.name)
  }

  const results = $("#search-results")
  results.on("click", "#song-results .fa", function(e) {
    const song = $(this).parent().data()
    const isPlay = e.target.classList.contains("fa-play")
    $.get("data/songs/" + song.file, e => playlist.add(e, isPlay))
  });
  results.on("click", "#album-results .fa", function(e) {
    const album = $(this).parent().data()
    $.get("data/albums/" + album.dir, e => playlist.add(e, false))
  })

  $("#searchbox").bind('input change', function(e) {
    const text = $(this).val()
    if (text === "") {
      $("#clear-results").click()
      return
    }
    $.get("search/" + text, e => setResults(Date.now(), e))
  });
  $("#search-results").tabs()
  $("#clear-results").click()
});
