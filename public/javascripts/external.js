$(function () {
  const external = $('#external');

  function showLinks(metaContent) {
    function addLinks(name) {
      const links = metaContent[name]
      const ul = $(`<ul>${name}</ul>`)
      for (const e in links) {
        const link = links[e]
        const hostName = link.name.replace(/\*$/g, "") // remove trailing "*" in order to fetch the correct icon
        // TODO add extensions
        ul.append($(`<li style="list-style-image: url('assets/images/${hostName}_icon.png')"><a target=_blank href=${link.main}>${link.name}</a></li>`))
      }
      external.append(ul)
    }

    external.html("")
    addLinks("artist")
    addLinks("album")
  }

  External.show = function (song) {
    external.html("Fetching links...");
    $.get("external/" + song.file, l => showLinks(l))
        .fail(function() {
      external.html("Error occurred while fetching links");
    });
  }
});
External = {};
