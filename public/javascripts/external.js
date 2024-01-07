$(function() {
  function href(target, name) {
    return `<a target=_blank href="${target}">${name}</a>`
  }

  const externalDivs = $(".external")

  let currentPosterRgb = []

  const externalArtist = $("#external-artist")
  const externalAlbum = $("#external-album")
  const artistReconBox =
      $("<input class='external-recon-id' placeholder='Artist ID' type='text'/>").appendTo(externalArtist)
  appendBr(externalArtist)
  const albumReconBox =
      $("<input class='external-recon-id' placeholder='Album ID' type='text'/>").appendTo(externalAlbum)
  appendBr(externalAlbum)
  const updateReconButton = button("Update Recon").appendTo(externalDivs)
  const refreshButton = button("Refresh").appendTo(externalDivs)
  const remotePath = "external/"

  function getExtensions(link) {
    const $ = Object.keys(link.extensions).map(k => href(link.extensions[k], k)).join(", ")
    return $ + (link.host.includes("*") ?
        `${$ === '' ? '' : ', '}<a class='copy-to-clipboard' href='javascript:void(0)' url='${link.main}'>copy</a>` :
        "")
  }

  function updateRecon() {
    const json = {}

    function addIfNotEmpty(elem) {
      const id = elem[0].placeholder.split(" ")[0].toLowerCase()
      const text = elem.val().takeAfterLast("/")
      if (text.length !== 0) {
        assert(RECON_REGEX.test(text))
        json[id] = text
      }
    }

    addIfNotEmpty(artistReconBox)
    addIfNotEmpty(albumReconBox)
    if (!isEmptyObject(json)) {
      const songPath = gplaylist.currentPlayingSong().file
      postJson(remotePath + "recons/" + songPath, json, showLinks(remotePath + songPath))
    }
  }

  function setLinkColor(e) {
    e.css("background-image", `linear-gradient(to top left, ${(rgb2String(makeLighter(currentPosterRgb, 0.5)))}, ${rgb2String(currentPosterRgb)})`)
  }

  function cleanUp() {
    externalDivs.children('ul').remove()
    externalDivs.children('span').remove()
  }

  // Yey, currying!
  function showLinks(debugLink) {
    cleanUp()
    externalDivs.prepend(span("Fetching links..."))

    function externalLinks(links) {
      cleanUp()
      artistReconBox.val("")
      albumReconBox.val("")
      $.each(links, (entityName, externalLinksForEntity) => {
        const isValid = externalLinksForEntity.timestamp
        const timestampOrError = `${entityName} (${isValid ?
            externalLinksForEntity.timestamp : href(debugLink, externalLinksForEntity.error)})`
        const ul = elem('ul', {'class': 'external-links'})
        if (isValid) {
          $.each(externalLinksForEntity, (linkName, link) => {
            if (linkName === "timestamp")
              return
            const extensions = getExtensions(link)
            const links = href(link.main, link.host) + (extensions ? ` (${extensions})` : "")
            const imageIcon = `"list-style-image: url('assets/images/${link.host.replace(/[*?].*$/g, "")}_icon.png')"`
            ul.append($(`<li style=${imageIcon}>${links}</li>`))
          })
        }
        const fieldset = $(`#external-${entityName.split(" ")[0].toLowerCase()}`)
        // TODO this shouldn't really be created every time
        fieldset.prepend(ul)
        fieldset.children('legend').remove()
        fieldset.prepend($(`<legend>${timestampOrError}</legend>`))
        setLinkColor(fieldset)
      })
    }

    return externalLinks
  }

  External.show = function(song) {
    const externalUrl = remotePath + song.file
    $.get(externalUrl, showLinks(externalUrl))
        .fail(function() {
          cleanUp()
          // FIXME A better error message
          externalDivs.append(span("Error occurred while fetching links"))
        })
  }

  const HEXA = "[a-f0-9]"
  // E.g., d8f63b51-73e0-4f65-8bd3-bcfe6892fb0e
  const RECON_REGEX = new RegExp(`^(.*/)?${HEXA}{8}-(?:${HEXA}{4}-){3}${HEXA}{12}$`)
  // Update recon on pressing Enter
  validateBoxAndButton($(".external-recon-id"), updateReconButton, s => RECON_REGEX.test(s), updateRecon)
  externalDivs.on("click", ".copy-to-clipboard", function() {
    copyTextToClipboard($(this).attr("url"))
  })
  refreshButton.click(() => {
    const songPath = gplaylist.currentPlayingSong().file
    $.get(remotePath + "refresh/" + songPath, showLinks(remotePath + songPath))
  })
  // TODO this is a hack to also handle all other fieldsets, probably shouldn't be in this file...
  Poster.rgbListeners.push(rgb => {
    currentPosterRgb = rgb
    $("#field-set-group fieldset").each(function() {
      setLinkColor($(this))
    })
  })
})
External = {}
