$(function () {
  const href = (target, name) => `<a target=_blank href=${target}>${name}</a>`

  const externalDiv = $("#external");

  function showLinks(externalLinks) {
    externalDiv.html("")
    $.each(externalLinks, (entityName, externalLinksForEntity) => {
      const ul = elem("ul", entityName)
      $.each(externalLinksForEntity, (_, link) => {
        const extensions = Object.keys(link.extensions).map(k => href(link.extensions[k], k)).join(", ")
        const links = href(link.main, link.host) + (extensions ? ` (${extensions})` : "")
        const imageIcon = `"list-style-image: url('assets/images/${link.host.replace(/\*$/g, "")}_icon.png')"`
        ul.append($(`<li style=${imageIcon}>${links}</li>`))
      })
      externalDiv.append(ul)
    })
  }

  External.show = function (song) {
    externalDiv.html("Fetching links...");
    $.get("external/" + song.file, l => showLinks(l))
        .fail(function () {
          externalDiv.html("Error occurred while fetching links");
        });
  }
});
External = {};
