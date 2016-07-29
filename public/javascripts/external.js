$(function () {
  const external = $('#external');

  function showLinks(metaContent) {
    function addLinks(name) {
      const links = metaContent[name]
      const ul = $(`<ul>${name}</ul>`)
      for (const e in links)
        ul.append($(`<li style="list-style-image: url('assets/images/${e}_icon.png')"><a href=${links[e]}>${e}</a></li>`))
      external.append(ul)
    }
    addLinks("artist")
    addLinks("album")
  }

  External.show = function (song) {
    external.html("Fetching links");
    $.get("external/" + song.file, l => showLinks(l));
  }
});
External = {};
