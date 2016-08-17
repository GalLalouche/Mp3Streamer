$(function () {
  const external = $('#external');

  function showLinks(metaContent) {
    function addLinks(name) {
      const links = metaContent[name]
      const ul = $(`<ul>${name}</ul>`)
      for (const e in links) {
        const hostName = e.replace(/\*$/g, "") // remove trailing "*" in order to fetch the correct icon
        ul.append($(`<li style="list-style-image: url('assets/images/${hostName}_icon.png')"><a target=_blank href=${links[e]}>${e}</a></li>`))
      }
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
