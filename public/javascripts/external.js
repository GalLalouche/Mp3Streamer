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
    if ($("#update-recon").prop("disabled"))
      return
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
    // TODO this shouldn't really be created every time
    externalDiv.append($("<input class='external-recon-id' id='artist-id' placeholder='Artist ID' type='text'/><br/>"))
    externalDiv.append($("<input class='external-recon-id' id='album-id' placeholder='Album ID' type='text'/><br/>"))
    externalDiv.append(elem("button", "Update Recon").click(updateRecon).attr("id", "update-recon").prop("disabled", true))
  }

  External.show = function (song) {
    resetLinks()
    const externalUrl = remotePath + song.file
    $.get(externalUrl, l => showLinks(l, externalUrl))
        .fail(function () {
          externalDiv.html("Error occurred while fetching links");
        });
  }

  const hexa = "[a-f0-9]"
  // d8f63b51-73e0-4f65-8bd3-bcfe6892fb0e
  const reconRegex = `${hexa}{8}-(?:${hexa}{4}-){3}${hexa}{12}`
  function verify(element) {
    const text = element.val();
    $("#update-recon").prop('disabled', !text.match(reconRegex))
  }
  externalDiv.on("click", ".copy-to-clipboard", function () {
    copyTextToClipboard($(this).attr("url"))
  })
  // Update recon on pressing Enter
  externalDiv.on("keyup", ".external-recon-id", function(event) {
    if (event.keyCode == 13) {
      updateRecon()
    } else {
      verify($(this))
    }
  })
});
External = {};
