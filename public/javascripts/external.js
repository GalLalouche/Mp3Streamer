$(function () {
  const external = $('#external');

  const href = (target, name) => `<a target=_blank href=${target}>${name}</a>`
  function showLinks(metaContent) {
    function addLinks(name) {
      const links = metaContent[name]
      const ul = $(`<ul>${name}</ul>`)
      for (const e in links) {
        const link = links[e]
        const hostName = link.host.replace(/\*$/g, "") // remove trailing "*" in order to fetch the correct icon
        // TODO add extensions
        const extensions = Object.keys(link.extensions).map(k => href(link.extensions[k], k)).join(", ")
        if (extensions)
          console.log(extensions)
        ul.append($(`<li style="list-style-image: url('assets/images/${hostName}_icon.png')">` +
          `${href(link.main, link.host)}${extensions ? ` (${extensions})` : ""}</li>`))
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
        .fail(function () {
          external.html("Error occurred while fetching links");
        });
  }
});
External = {};
