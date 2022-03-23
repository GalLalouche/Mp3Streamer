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

const elemFactory = e => config => elem(e, config)
const button = (config, text) => elem("button", config, text)
const span = config => elem("span", config)
const li = config => elem("li", config)
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
  return this
}
