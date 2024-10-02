function isEmptyObject(obj: object): boolean {
    for (const prop in obj)
        if (Object.prototype.hasOwnProperty.call(obj, prop))
            return false
    return true
}

const _URL_PATTERN: RegExp = new RegExp('^(https?:\\/\\/)?' + // protocol
    '((([a-z\\d]([a-z\\d-]*[a-z\\d])*)\\.?)+[a-z]{2,}|' + // domain name
    '((\\d{1,3}\\.){3}\\d{1,3}))' + // OR ip (v4) address
    '(\\:\\d+)?.*') // port and path
function isValidUrl(urlString: string): boolean {
    return _URL_PATTERN.test(urlString)
}

// Copied from https://stackoverflow.com/a/30810322/736508
// Comments removed for brevity.
function copyTextToClipboard(text: string): void {
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

function newNotification(title: string, body: string): Notification {
    return new Notification(title, {body: body})
}

function showDesktopNotification(title: string, body: string, timelimitInSeconds?: number) {
    let notification: Notification | null = null
    if (!window.Notification)
        console.log('Browser does not support notifications.')
    if (Notification.permission === 'granted') {
        notification = newNotification(title, body)
        return
    }
    Notification.requestPermission()
        .then(p => {
            if (p === 'granted') notification = newNotification(title, body)
            else console.log('User blocked notifications.')
        })
        .catch(err => console.error(err))

    if (timelimitInSeconds !== undefined)
        setTimeout(() => {
            if (notification) notification.close()
        }, timelimitInSeconds * 1000)
}

function map_values(o: object, f: (arg0: any) => any): object {
    let result: object = {}
    for (const key in o) {
        result[key] = f(o[key])
    }
    return result
}

function not(b: boolean): boolean {
    return !b
}

type ObjectIndex<K extends string | number | symbol, V> = {
    [P in K]: V
};

interface Array<T> {
    custom_last(): T
    custom_sort_by<S>(f: (a: T) => S): T[]
    custom_group_by<S extends string | number | symbol>(f: (a: T) => S): ObjectIndex<S, T[]>
    custom_max(): T | null
}

Array.prototype.custom_last = () => this [this.length - 1]
Array.prototype.custom_sort_by = f =>
    this
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
Array.prototype.custom_group_by = function <S extends string | number | symbol, T>(f: (a: T) => S) {
    let result: ObjectIndex<S, T[]> = {} as unknown as ObjectIndex<S, T[]>
    this.forEach((e: T) => {
        const key = f(e)
        if (result[key] === undefined)
            result[key] = []
        result[key].push(e)
    })
    return result
}
Array.prototype.custom_max = function () {
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

function assert(condition: boolean, message?: string) {
    if (condition)
        return
    message = message || "Assertion failed"
    throw typeof Error === "undefined" ? message : new Error(message)
}

class AssertionError extends Error {
    constructor(message?: string) {
        super(message || "AssertionError")
    }
}

interface Number {
    timeFormat(): string
    withMinDigits(n: number): string
}

Number.prototype.withMinDigits = function (n: number): string {
    return this.toLocaleString('en-US', {minimumIntegerDigits: n})
}
Number.prototype.timeFormat = function () {
    const hours = Math.floor(this / 3600)
    const minutes = Math.floor((this - (hours * 3600)) / 60)
    const seconds = Math.round(this - (hours * 3600) - (minutes * 60))

    const hourPrefix = hours == 0 ? "" : hours.withMinDigits(2)
    return hourPrefix + minutes.withMinDigits(2) + ':' + seconds.withMinDigits(2)
}

interface String {
    takeAfterLast(substring: string): string
}

String.prototype.takeAfterLast = function (subs) {
    return this.substring(this.lastIndexOf(subs) + 1)
}

interface Boolean {
    isFalse(): boolean
}

Boolean.prototype.isFalse = function () {
    return not(this.valueOf())
}

const ENTER_CODE: number = 13
