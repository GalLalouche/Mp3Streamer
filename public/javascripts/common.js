String.prototype.takeAfterLast = function(subs) {
  return this.substr(this.lastIndexOf(subs) + 1)
}
Boolean.prototype.isFalse = function() {
  return !this.valueOf();
}

const not = b => !b

Number.prototype.timeFormat = function() {
  let hours = Math.floor(this / 3600)
  let minutes = Math.floor((this - (hours * 3600)) / 60)
  let seconds = Math.round(this - (hours * 3600) - (minutes * 60))

  hours = (0 < hours && hours < 10 ? "0" : "") + hours
  if (hours < 10) hours = "0" + hours
  if (minutes < 10) minutes = "0" + minutes
  if (seconds < 10) seconds = "0" + seconds

  const hourPrefix = hours === "00" ? "" : hours + ":"
  return hourPrefix + minutes + ':' + seconds
}
// Copied from https://stackoverflow.com/a/30810322/736508
// Comments removed for brevity.
function copyTextToClipboard(text) {
  const textArea = document.createElement("textarea")
  textArea.style.position = 'fixed'
  textArea.style.top = '0'
  textArea.style.left = '0'
  textArea.style.width = '2em'
  textArea.style.height = '2em'
  textArea.style.padding = '0'
  textArea.style.border = 'none'
  textArea.style.outline = 'none'
  textArea.style.boxShadow = 'none'
  textArea.style.background = 'transparent'
  textArea.value = text

  document.body.appendChild(textArea)

  textArea.select()

  // TODO this is deprecated, but actually works right now so ¯\_(ツ)_/¯
  try {
    document.execCommand('copy')
  } catch (ignored) {
    console.log('Oops, unable to copy')
  }

  document.body.removeChild(textArea)
}

function isEmptyObject(obj) {
  for (const prop in obj)
    if (Object.prototype.hasOwnProperty.call(obj, prop))
      return false
  return true
}

const _URL_PATTERN = new RegExp('^(https?:\\/\\/)?' + // protocol
    '((([a-z\\d]([a-z\\d-]*[a-z\\d])*)\\.?)+[a-z]{2,}|' + // domain name
    '((\\d{1,3}\\.){3}\\d{1,3}))' + // OR ip (v4) address
    '(\\:\\d+)?.*') // port and path
function isValidUrl(urlString) {
  return _URL_PATTERN.test(urlString)
}

const ENTER_CODE = 13
Array.prototype.custom_last = function() {
  return this[this.length - 1]
}
Array.prototype.custom_sort_by = function(f) {
  return this
      .map(e => [e, f(e)])
      .sort((a, b) => {
        const fa = a[1]
        const fb = b[1]
        if (fa === fb)
          return 0
        if (fa < fb)
          return -1
        else
          return 1
      })
      .map(e => e[0])
}
Array.prototype.custom_group_by = function(f) {
  let result = {}
  this.forEach(e => {
    const key = f(e)
    if (result[key] === undefined)
      result[key] = []
    result[key].push(e)
  })
  return result
}
Array.prototype.custom_max = function() {
  let max = null
  for (const e of this) {
    if (max === null) {
      max = e
    } else {
      max = max > e ? max : e
    }
  }
  return max
}
// Adding this is a prototype method to Object messes things up for some reason :\
function map_values(o, f) {
  let result = {}
  for (const [key, value] of Object.entries(o))
    result[key] = f(value)
  return result
}

function assert(condition, message) {
  if (condition)
    return
  if (not(message))
    return assert(condition, "Assertion failed")
  throw typeof Error === "undefined" ? message : new Error(message)
}
class AssertionError extends Error {
  constructor(message) {
    super(message || "AssertionError")
  }
}