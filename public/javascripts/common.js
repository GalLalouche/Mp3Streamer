$(function() {
  function ajaxJson(method, url, data, success) {
    $.ajax({
      url: url,
      data: typeof data === 'string' ? data : JSON.stringify(data),
      type: method,
      contentType: "application/json; charset=utf-8",
      dataType: "json",
      success: success,
      statusCode: {
        201: success
      }
    })
  }

  // noinspection JSUndeclaredVariable
  postJson = function(url, data, success) {
    ajaxJson("POST", url, data, success)
  }

  // noinspection JSUndeclaredVariable
  putJson = function(url, data, success) {
    ajaxJson("PUT", url, data, success)
  }
})

function elem(elementName, config, innerText) {
  if (innerText) {
    // If innerText is given, config has to be a config, otherwise it can be an innerText
    assert(typeof config === 'object' && typeof innerText === 'string')
    return $(`<${elementName}/>`, config).html(innerText)
  }

  if (typeof config === 'object')
    return $(`<${elementName}/>`, config)

  return $(`<${elementName}>${config || ""}</${elementName}>`)
}
String.prototype.takeAfterLast = function(subs) {
  return this.substr(this.lastIndexOf(subs) + 1)
}
Boolean.prototype.isFalse = function() {
  return !this.valueOf();
}
const not = b => !b

const elemFactory = e => config => elem(e, config)
const button = (config, text) => elem("button", config, text)
const span = config => elem("span", config)
const div = elemFactory('div')
const img = src => elem('img').attr("src", src)
const br = elemFactory('br')
const icon = name => `<i class="fa fa-${name}"/>`
const table = elemFactory('table')
const td = elemFactory('td')
const tr = elemFactory('tr')

function appendBr(elementToAppendTo) {
  elementToAppendTo.append(br())
}

Number.prototype.timeFormat = function() {
  let hours = Math.floor(this / 3600)
  let minutes = Math.floor((this - (hours * 3600)) / 60)
  let seconds = this - (hours * 3600) - (minutes * 60)

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
  textArea.style.top = 0
  textArea.style.left = 0
  textArea.style.width = '2em'
  textArea.style.height = '2em'
  textArea.style.padding = 0
  textArea.style.border = 'none'
  textArea.style.outline = 'none'
  textArea.style.boxShadow = 'none'
  textArea.style.background = 'transparent'
  textArea.value = text

  document.body.appendChild(textArea)

  textArea.select()

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

jQuery.each(["put", "delete"], function(i, method) {
  jQuery[method] = function(url, data, callback, type) {
    if (jQuery.isFunction(data))
      return arguments.callee(url, undefined, data, type || callback)

    return jQuery.ajax({
      url: url,
      type: method,
      dataType: type,
      data: data,
      success: callback
    })
  }
})

const _URL_PATTERN = new RegExp('^(https?:\\/\\/)?' + // protocol
    '((([a-z\\d]([a-z\\d-]*[a-z\\d])*)\\.?)+[a-z]{2,}|' + // domain name
    '((\\d{1,3}\\.){3}\\d{1,3}))' + // OR ip (v4) address
    '(\\:\\d+)?.*') // port and path
function isValidUrl(urlString) {
  return _URL_PATTERN.test(urlString)
}

/**
 * Disables or enables a button if the textbox contains valid text. Also binds "ENTER" key press to clicking the button.
 * @param textBox Jquery textbox(es) to bind a keyup event to; when modified, its text will be validated
 * @param button Jquery button which will be disabled or enabled, if the above textbox is successfully validated
 *               will also be bound to the button function, and will be disabled by default
 * @param validationFunction A function that accepts a string (the new contents of the modified text box) and returns
 *                           true if the content is valid
 * @param buttonFunction A function to invoke when the button is clicked (assuming is is enabled, of course), or when
 *                       the 'Enter' (<CR>) key is pressed while modifying the textbox(es)
 */
function validateBoxAndButton(textBox, button, validationFunction, buttonFunction) {
  function runIfEnabled() {
    if (button.prop("disabled"))
      return
    buttonFunction()
  }

  const ENTER_CODE = 13
  button.click(() => runIfEnabled())
  button.prop('disabled', true)
  textBox.keyup(function(event) {
    if (event.keyCode === ENTER_CODE)
      runIfEnabled()
    else
      button.prop('disabled', false === validationFunction($(this).val()))
  })
}

$.fn.custom_overflown = function() {
  const e = this[0]
  return e.scrollHeight > e.clientHeight || e.scrollWidth > e.clientWidth
}
$.fn.custom_tooltip = function(text) {
  this.attr('title', text)
}
Array.prototype.custom_last = function() {
  return this[this.length - 1]
}

function assert(condition, message) {
  if (condition)
    return
  if (not(message))
    return assert(condition, "Assertion failed")
  throw typeof Error === "undefined" ? message : new Error(message)
}
