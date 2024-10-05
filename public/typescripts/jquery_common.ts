type Method = "POST" | "PUT"

function ajaxJson(method: Method, url: string, data: object, success?: any): void {
    $.ajax({
        url: url,
        data: typeof data === 'string' ? data : JSON.stringify(data),
        type: method,
        contentType: "application/json; charset=utf-8",
        dataType: "json",
        success: success,
        statusCode: {
            201: success,
        },
    })
}

function postJson(url: string, data: object, success?: any): void {
    ajaxJson("POST", url, data, success)
}

function putJson(url: string, data: object, success?: any): void {
    ajaxJson("PUT", url, data, success)
}

jQuery.each(["put", "delete"], function (i, method) {
    function func(url: string, data: object, callback: any, type: any): any {
        return isFunction(data)
            ? arguments.callee(url, undefined, data, type || callback)
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
function elem(elementName: string, configOrInnerText: object | string): JQuery<HTMLElement>
function elem(elementName: string, config: object, innerText: string): JQuery<HTMLElement>

function elem(elementName: string, config?: object | string, innerText?: string): JQuery<HTMLElement> {
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
const button = (config: object, text?: string) => elem("button", config, text)
const span = (config: object) => elem("span", config)
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
    validationFunction: (s: string | number | string[]) => boolean,
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
            button.prop('disabled', false === validationFunction($(this).val()))
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
