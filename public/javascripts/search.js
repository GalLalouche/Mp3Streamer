$(function() {
  function setResults(requestTime, jsArray) {
    $("#search-results").show()
    function img(name) {
      const size = 24
      return `<img src="assets/images/${name}_icon.png"
          height="${size}" width="${size}"
          class="result-list-button ${name}" />`
    }
    function specificResults(name, itemProducer) {
      const tab = $(`#${name}-results`)
      tab.empty()
      tab.append('<ul/>')
      const ul = tab.find('ul')
      $.each(jsArray[`${name}s`], function(_, s) {
        const item = itemProducer(s)
        ul.append(`<li data-${name}='${JSON.stringify(s)}'>${item}</li>`)
      })
    }
    const results = $("#search-results")
    if (results.attr("time") > requestTime)
      return // a later request has already set the result
    results.attr("time", requestTime)
    specificResults("song",  s => `${img("play")} ${img("add")} ${s.artistName}: ${s.title}`)
    specificResults("album", a => `${img("play")} ${img("add")} ${a.artistName}: ${a.title}`)
    specificResults("artist", a => a.name)
  }

  $("#search-results").on("click", ".result-list-button", function(e) {
    const song = JSON.parse(e.target.parentElement.attributes["data-song"].value)
    const isPlay = e.target.classList.contains("play")
    $.get("data/songs/" + song.file, e => playlist.add(e, isPlay))
  });

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
