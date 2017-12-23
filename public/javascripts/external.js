$(function() {
  const href = (target, name) => `<a target=_blank href="${target}">${name}</a>`
  const externalDivParent = $("#external")
  const externalDiv = div().appendTo(externalDivParent)

  const artistReconBox =
      $("<input class='external-recon-id' placeholder='Artist ID' type='text'/>").appendTo(externalDivParent)
  appendBr(externalDivParent)
  const albumReconBox =
      $("<input class='external-recon-id' placeholder='Album ID' type='text'/>").appendTo(externalDivParent)
  appendBr(externalDivParent)
  const updateReconButton = button("Update Recon").appendTo(externalDivParent)
  const refreshButton = button("Refresh").appendTo(externalDivParent)
  const remotePath = "external/"

  function getExtensions(link) {
    const $ = Object.keys(link.extensions).map(k => href(link.extensions[k], k)).join(", ")
    return $ + (link.host.endsWith("*") ?
        `${$ === '' ? '' : ', '}<a class='copy-to-clipboard' href='javascript:void(0)' url='${link.main}'>copy</a>` :
        "")
  }

  function updateRecon() {
    const json = {}

    function addIfNotEmpty(elem) {
      const id = elem[0].placeholder.split(" ")[0].toLowerCase()
      const text = elem.val()
      if (text.length !== 0) // the box is either empty, or is valid TODO replace with an assert
        json[id] = text
    }

    addIfNotEmpty(artistReconBox)
    addIfNotEmpty(albumReconBox)
    if (!isEmptyObject(json)) {
      const songPath = gplaylist.currentPlayingSong().file
      postJson(remotePath + "recons/" + songPath, json, showLinks(remotePath + songPath))
    }
  }

  // Yey, currying!
  const showLinks = debugLink => {
    externalDiv.html("Fetching links...")
    return externalLinks => {
      artistReconBox.val("")
      albumReconBox.val("")
      externalDiv.html("")
      $.each(externalLinks, (entityName, externalLinksForEntity) => {
        const isValid = externalLinksForEntity.timestamp
        const timestampOrError = `${entityName} (${isValid ?
            externalLinksForEntity.timestamp : href(debugLink, externalLinksForEntity.error)})`
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
  }

  External.show = function(song) {
    const externalUrl = remotePath + song.file
    $.get(externalUrl, showLinks(externalUrl))
        .fail(function() {
          externalDiv.html("Error occurred while fetching links")
        })
  }

  const hexa = "[a-f0-9]"
  // E.g., d8f63b51-73e0-4f65-8bd3-bcfe6892fb0e
  const reconRegex = new RegExp(`^${hexa}{8}-(?:${hexa}{4}-){3}${hexa}{12}$`)
  // Update recon on pressing Enter
  validateBoxAndButton($(".external-recon-id"), updateReconButton, s => reconRegex.test(s), updateRecon)
  externalDiv.on("click", ".copy-to-clipboard", function() {
    copyTextToClipboard($(this).attr("url"))
  })
  refreshButton.click(() => {
    const songPath = gplaylist.currentPlayingSong().file
    $.get(remotePath + "refresh/" + songPath, showLinks(remotePath + songPath))
  })
})
External = {}
