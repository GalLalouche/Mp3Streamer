$(function () {
  const external = $('#external');

  function showLinks(metaContent) {
    function addLinks(name, content) {
      const links = content[name]
      const ul = $(`<ul>${name}</ul>`)
      for (const e in links)
        ul.append($(`<li style="list-style-image: url('assets/images/${e}_icon.png')"><a href=${content[e]}>${e}</a></li>`))
      external.append(ul)
    }
    addLinks("artist")
    addLinks("album")
  }

  External.show = function (song) {
    external.html("External links has been temporarily disabled.");
    // $.get("external/" + song.file, l => showLinks(l));
  }
});
External = {};
