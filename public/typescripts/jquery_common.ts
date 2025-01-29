// TODO this should be turned into a proper module probably, or at least add a comment as to why it isn't.
import jqXHR = JQuery.jqXHR

type Method = "POST" | "PUT"

function ajaxJson(method: Method, url: string, data: object | string, success?: any): jqXHR<any> {
  return $.ajax({
    url: url,
    data: isString(data) ? data : JSON.stringify(data),
    type: method,
    contentType: "application/json; charset=utf-8",
    success: success,
    statusCode: {
      201: success,
    },
  })
}

function postJson(url: string, data: object, success?: any): jqXHR<any> {
  return ajaxJson("POST", url, data, success)
}

function putJson(url: string, data: object, success?: any): jqXHR<any> {
  return ajaxJson("PUT", url, data, success)
}

interface JQueryStatic {
  put(url: string, success?: (o: any) => void): void
  put(url: string, data: object, success?: (o: any) => void): void
  put(url: string, data: object, typ?: string, success?: (o: any) => void): void
  delete(url: string, success?: (o: any) => void): void
  delete(url: string, data: object, success?: (o: any) => void): void
  delete(url: string, data: object, typ?: string, success?: (o: any) => void): void
}

jQuery.each(["put", "delete"], function (_i, method) {
  function func(url: string, data: object | undefined, callback: any, type?: string): any {
    return isFunction(data)
      ? func(url, undefined, data, type || callback)
      : jQuery.ajax({
        url: url,
        type: method,
        dataType: type,
        data: data,
        success: callback,
      })
  }

  // @ts-expect-error
  jQuery[method] = func
})

function elem(elementName: string): JQuery<HTMLElement>
function elem(elementName: string, configOrInnerText?: object | string): JQuery<HTMLElement>
function elem(elementName: string, config: object, innerText?: string): JQuery<HTMLElement>
function elem(elementName: string, config?: object | string, innerText?: string): JQuery<HTMLElement> {
  return _elemImpl(elementName, config, innerText)
}

function _elemImpl(elementName: string, config?: object | string, innerText?: string): JQuery<HTMLElement> {
  if (innerText) {
    if (isObject(config))
      return $(`<${elementName}/>`, config).html(innerText)
    else
      throw new AssertionError(
        `config should have been an object if innerText is passed, was ${typeof config}`,
      )
  }

  if (isObject(config))
    return $(`<${elementName}/>`, config)

  return $(`<${elementName}>${config || ""}</${elementName}>`)
}

const elemFactory = (e: string) => (config?: object) => elem(e, config)

function button(text: string): JQuery<HTMLElement>
function button(config: object, text: string): JQuery<HTMLElement>
function button(config?: object | string, innerText?: string): JQuery<HTMLElement> {
  return _elemImpl("button", config, innerText)
}

const span = (configOrInnerText: object | string) => elem("span", configOrInnerText)
const li = (config: object) => elem("li", config)
const div = elemFactory('div')
const img = (src: string) => elem('img').attr("src", src)
const br = elemFactory('br')
const icon = (name: string) => `<i class="fa fa-${name}"/>`
const table = elemFactory('table')
const td = elemFactory('td')
const tr = elemFactory('tr')


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
function validateBoxAndButton(
  textBox: JQuery<HTMLElement>, button: JQuery<HTMLElement>,
  validationFunction: (s: string) => boolean,
  buttonFunction: () => void,
): void {
  function runIfEnabled() {
    if (button.prop("disabled").isFalse())
      buttonFunction()
  }

  button.click(() => runIfEnabled())
  button.prop('disabled', true)
  textBox.keyup(function (event) {
    if (event.keyCode === ENTER_CODE)
      runIfEnabled()
    else
      button.prop('disabled', validationFunction($(this).val() as string).isFalse())
  })
}

interface JQuery {
  custom_overflown(): boolean
  custom_tooltip(text: string): JQuery
  appendBr(): JQuery
}

$.fn.custom_overflown = function () {
  const e = this[0]
  return e.scrollHeight > e.clientHeight || e.scrollWidth > e.clientWidth
}
$.fn.custom_tooltip = function (text) {
  this.attr('title', text)
  return this
}

$.fn.appendBr = function () {
  this.append(br())
  return this
}
