$(function () {
  const href = (target, name) => `<a target=_blank href="${target}">${name}</a>`

  const externalDiv = $("#external");

  function getExtensions(link) {
    const $ = Object.keys(link.extensions).map(k => href(link.extensions[k], k)).join(", ")
    return $ + (link.host.endsWith("*") ?
        `${$ === '' ? '' : ', '}<a class='copy-to-clipboard' href='javascript:void(0)' url='${link.main}'>copy</a>` :
        "")
  }

  const formatTimestamp = s => `${s.slice(6)}/${s.slice(4, 6)}/${s.slice(0, 4)}`

  function showLinks(externalLinks, debugLink) {
    externalDiv.html("")
    $.each(externalLinks, (entityName, externalLinksForEntity) => {
      const isValid = externalLinksForEntity.timestamp
      const timestampOrError = `${entityName} (${isValid ?
          formatTimestamp(externalLinksForEntity.timestamp) : href(debugLink, externalLinksForEntity.error)})`
      const ul = elem("ul", timestampOrError)
      if (isValid) {
        $.each(externalLinksForEntity, (linkName, link) => {
          if (linkName == "timestamp")
            return
          const extensions = getExtensions(link)
          const links = href(link.main, link.host) + (extensions ? ` (${extensions})` : "")
          const imageIcon = `"list-style-image: url('assets/images/${link.host.replace(/[*?]$/g, "")}_icon.png')"`
          ul.append($(`<li style=${imageIcon}>${links}</li>`))
        })
      }
      externalDiv.append(ul)
    })
  }

  External.show = function (song) {
    const externalUrl = "external/" + song.file
    externalDiv.html("Fetching links...");
    $.get(externalUrl, l => showLinks(l, externalUrl))
        .fail(function () {
          externalDiv.html("Error occurred while fetching links");
        });
  }
  externalDiv.on("click", ".copy-to-clipboard", function () {
    const foo = $(this)
    copyTextToClipboard(foo.attr("url"))
  })
});
External = {};
