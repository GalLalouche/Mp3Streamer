$(function () {
  const href = (target, name) => `<a target=_blank href="${target}">${name}</a>`
  const externalDivParent = $("#external")
  const externalDiv = elem('div').appendTo(externalDivParent)

  const artistReconBox =
      $("<input class='external-recon-id' placeholder='Artist ID' type='text'/><br/>").appendTo(externalDivParent)
  const albumReconBox =
      $("<input class='external-recon-id' placeholder='Album ID' type='text'/><br/>").appendTo(externalDivParent)
  const updateReconButton = elem("button", "Update Recon").appendTo(externalDivParent)
  const remotePath = "external/"

  function getExtensions(link) {
    const $ = Object.keys(link.extensions).map(k => href(link.extensions[k], k)).join(", ")
    return $ + (link.host.endsWith("*") ?
        `${$ === '' ? '' : ', '}<a class='copy-to-clipboard' href='javascript:void(0)' url='${link.main}'>copy</a>` :
        "")
  }

  function resetLinks() {
    externalDiv.html("Fetching links...")
  }

  const formatTimestamp = s => `${s.slice(6)}/${s.slice(4, 6)}/${s.slice(0, 4)}`

  function updateRecon() {
    const json = {}
    function addIfNotEmpty(elem) {
      const text = elem.val()
      if (text.length !== 0) // the box is either empty, or is valid TODO replace with an assert
        json[id] = text
    }
    addIfNotEmpty(artistReconBox)
    addIfNotEmpty(albumReconBox)
    if (!isEmptyObject(json)) {
      resetLinks()
      const songPath = gplaylist.currentPlayingSong().file
      postJson(remotePath + "recons/" + songPath, json, l => showLinks(l, remotePath + songPath))
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
          if (linkName === "timestamp")
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
  }

  External.show = function (song) {
    resetLinks()
    const externalUrl = remotePath + song.file
    $.get(externalUrl, l => showLinks(l, externalUrl))
        .fail(function () {
          externalDiv.html("Error occurred while fetching links")
        })
  }

  const hexa = "[a-f0-9]"
  // d8f63b51-73e0-4f65-8bd3-bcfe6892fb0e
  const reconRegex = new RegExp(`^${hexa}{8}-(?:${hexa}{4}-){3}${hexa}{12}$`)
  // Update recon on pressing Enter
  validateBoxAndButton($(".external-recon-id"), updateReconButton, s => reconRegex.test(s), updateRecon)
  externalDiv.on("click", ".copy-to-clipboard", function () {
    copyTextToClipboard($(this).attr("url"))
  })
})
External = {}
