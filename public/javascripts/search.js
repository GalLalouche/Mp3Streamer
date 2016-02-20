$(function() {
  function setResults(requestTime, jsArray) {
    function img(name) {
      const size = 24
      return `<img src="assets/images/${name}_icon.png"
          height="${size}" width="${size}"
          class="result-list-button ${name}" />`
    }
    const results = $("#search-results")
    if (results.attr("time") > requestTime)
      return // a later request has already set the result
    results.attr("time", requestTime)
    results.empty()
    results.append('<ul style="list-style-type:none" />')
    const ul = results.find('ul')
    $.each(jsArray, function(_, s) {
      const item = `${img("play")} ${img("add")} ${s.artist}: ${s.title}`
      ul.append(`<li data-song='${JSON.stringify(s)}'>${item}</li>`)
    })
  }

  $("#search-results").on("click", ".result-list-button", function(e) {
    const song = JSON.parse(e.target.parentElement.attributes["data-song"].value)
    const isPlay = e.target.classList.contains("play")
    $.get("data/songs/" + song.file, e => playlist.add(e, isPlay))
  });

  $("#searchbox").bind('input change', function(e) {
    const now = Date.now()
    var text = $(this).val()
    $.get("search/" + text, e => setResults(now, e))
  });
});
