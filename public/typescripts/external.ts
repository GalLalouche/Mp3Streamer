import {gplaylist, Song} from "./types.js"
import {match} from 'ts-pattern'
import {Poster} from "./poster.js"

export namespace External {
  export function show(song: Song): void {
    const externalUrl = remotePath + song.file
    const helper = getHelper()
    $.get(externalUrl, helper.showLinks(externalUrl))
      .fail(function () {
        helper.cleanUp()
        // FIXME A better error message
        helper.externalDivs.append(span("Error occurred while fetching links"))
      })
  }
}

const remotePath = "external/"
const HEXA = "[a-f0-9]"
// E.g., d8f63b51-73e0-4f65-8bd3-bcfe6892fb0e
const RECON_REGEX = new RegExp(`^(.*/)?${HEXA}{8}-(?:${HEXA}{4}-){3}${HEXA}{12}$`)

function href(target: string, name: string): string {
  return `<a target=_blank href="${target}">${name}</a>`
}

const getHelper = lazy(() => new Helper)

class Helper {
  readonly externalDivs = $(".external")
  readonly externalAlbum = $("#external-album")
  readonly externalArtist = $("#external-artist")
  readonly artistReconBox: JQuery<HTMLElement>
  readonly albumReconBox: JQuery<HTMLElement>
  currentPosterRgb: RGB | null = null
  constructor() {
    this.artistReconBox =
      $("<input class='external-recon-id' placeholder='Artist ID' type='text'/>")
        .appendTo(this.externalArtist)
    this.externalArtist.appendBr()
    this.albumReconBox =
      $("<input class='external-recon-id' placeholder='Album ID' type='text'/>")
        .appendTo(this.externalAlbum)
    const that = this
    this.externalAlbum.appendBr()
    const updateReconButton = button("Update Recon").appendTo(this.externalDivs)
    button("Refresh").appendTo(this.externalArtist).click(this.refresh("artist").bind(this))
    button("Refresh").appendTo(this.externalAlbum).click(this.refresh("album").bind(this))
    // Update recon on pressing Enter
    validateBoxAndButton(
      $(".external-recon-id"),
      updateReconButton,
      s => RECON_REGEX.test(s),
      this.updateRecon.bind(this),
    )
    this.externalDivs.on("click", ".copy-to-clipboard", function () {
      copyTextToClipboard($(this).attr("url")!)
    })

    // TODO this is a hack to also handle all other fieldsets, probably shouldn't be in this file...
    Poster.rgbListeners.push(rgb => {
      this.currentPosterRgb = rgb
      $("#field-set-group fieldset").each(function () {
        that.setLinkColor($(this))
      })
    })
  }
  refresh(target: string): () => void {
    const that = this
    return function () {
      const songPath = gplaylist.currentPlayingSong().file
      // TODO showLinks should only fetch the links for the target.
      $.get(`${remotePath}refresh/${target}/${songPath}`, that.showLinks(remotePath + songPath))
    }
  }
  cleanUp() {
    this.externalDivs.children('ul').remove()
    this.externalDivs.children('span').remove()
  }
  setLinkColor(e: JQuery<HTMLElement>): void {
    if (!this.currentPosterRgb)
      return
    const c1 = this.currentPosterRgb.makeLighter(0.5).toString()
    const c2 = this.currentPosterRgb.toString()
    e.css("background-image", `linear-gradient(to top left, ${c1}, ${c2})`)
  }
  // Yey, currying!
  showLinks(debugLink: string): ((r: ExternalResult) => void) {
    this.cleanUp()
    this.externalDivs.prepend(span("Fetching links..."))
    const that = this

    function externalLinks(result: ExternalResult) {
      that.cleanUp()
      that.artistReconBox.val("")
      that.albumReconBox.val("")
      Object.entries(result).forEach(([entityName, externalLinksForEntity]) => {
        const ul = elem('ul', {'class': 'external-links'})
        const finalLine: string =
          match(externalLinksForEntity)
            .returnType<string>()
            .when(isLinks, l => {
              $.each(l.links, (linkName, link) => {
                const extensions = getExtensions(link)
                const links = href(link.main, link.host) + (extensions ? ` (${extensions})` : "")
                const imageIcon = `"list-style-image: url('assets/images/${link.host.replace(/[*?].*$/g, "")}_icon.png')"`
                ul.append($(`<li style=${imageIcon}>${links}</li>`))
              })
              return l.timestamp
            })
            .when(isError, e => href(debugLink, e.error))
            .exhaustive()
        const fieldset = $(`#external-${entityName.split(" ")[0].toLowerCase()}`)
        // TODO this shouldn't really be created every time
        fieldset.prepend(ul)
        fieldset.children('legend').remove()
        fieldset.prepend($(`<legend>${entityName} (${finalLine})</legend>`))
        that.setLinkColor(fieldset)
      })
    }

    return externalLinks
  }
  updateRecon(): void {
    const json: Record<string, string> = {}

    function addIfNotEmpty(elem: JQuery<HTMLElement>) {
      const id = (elem[0] as any).placeholder.split(" ")[0].toLowerCase()
      const text = (elem.val() as string).takeAfterLast("/")
      if (text.length !== 0) {
        assert(RECON_REGEX.test(text))
        json[id] = text
      }
    }

    addIfNotEmpty(this.artistReconBox)
    addIfNotEmpty(this.albumReconBox)
    if (!isEmptyObject(json)) {
      const songPath = gplaylist.currentPlayingSong().file
      postJson(remotePath + "recons/" + songPath, json, this.showLinks(remotePath + songPath))
    }
  }
}

interface Links {
  timestamp: string
  links: Record<string, Link>
}

interface ExternalError {
  readonly error: string
}

type Result = Links | ExternalError

function isError(r: Result): r is ExternalError {return 'error' in r}

function isLinks(r: Result): r is Links {return 'timestamp' in r}

type ExternalResult = Record<string, Result>

interface Link {
  extensions: Record<string, string>
  main: string
  host: string
}

function getExtensions(link: Link): string {
  const $ = Object.keys(link.extensions).map(k => href(link.extensions[k], k)).join(", ")
  return $ + (link.host.includes("*") ?
    `${$ === '' ? '' : ', '}<a class='copy-to-clipboard' href='javascript:void(0)' url='${link.main}'>copy</a>` :
    "")
}
