$(function () {
  const href = (target, name) => `<a target=_blank href="${target}">${name}</a>`
  const externalDiv = $("#external");
  const remotePath = "external/"

  function getExtensions(link) {
    const $ = Object.keys(link.extensions).map(k => href(link.extensions[k], k)).join(", ")
    return $ + (link.host.endsWith("*") ?
        `${$ === '' ? '' : ', '}<a class='copy-to-clipboard' href='javascript:void(0)' url='${link.main}'>copy</a>` :
        "")
  }

  function resetLinks() {
    externalDiv.html("Fetching links...");
  }

  const formatTimestamp = s => `${s.slice(6)}/${s.slice(4, 6)}/${s.slice(0, 4)}`

  function updateRecon() {
    function addIfNotEmpty(json, id) {
      const text = $(`#${id}-id`).val()
      if (text.length != 0)
        json[id] = text
    }
    const json = {}
    addIfNotEmpty(json, "artist")
    addIfNotEmpty(json, "album")
    if (!isEmptyObject(json)) {
      resetLinks()
      const songPath = gplaylist.currentPlayingSong().file
      $.post(remotePath + "recons/" + songPath, JSON.stringify(json), l => showLinks(l, remotePath + songPath))
    }
  }

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
    externalDiv.append($("<input id='artist-id' placeholder='Artist ID' type='text'/><br/>"))
    externalDiv.append($("<input id='album-id' placeholder='Album ID' type='text'/><br/>"))
    externalDiv.append(elem("button", "Update Recon").click(updateRecon))
  }

  External.show = function (song) {
    resetLinks()
    const externalUrl = remotePath + song.file
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
