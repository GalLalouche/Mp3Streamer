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

function lazy<A>(ctor: () => A): () => A {
  let obj: A | undefined = undefined
  return () => obj || (obj = ctor())
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

function map_values(o: Record<string, any>, f: (arg0: any) => any): object {
  let result: Record<string, any> = {}
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

Array.prototype.custom_last = function () {return this [this.length - 1]}
Array.prototype.custom_sort_by = function <S, T>(f: (a: T) => S): T[] {
  return this
    .map((e: T): [T, S] => [e, f(e)])
    .sort((a: [T, S], b: [T, S]) => {
      const fa = a[1]
      const fb = b[1]
      if (fa === fb)
        return 0
      if (fa < fb)
        return -1
      else
        return 1
    })
    .map((e: [T, S]) => e[0])
}
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
  let max: number | null = null
  for (const e of this) {
    if (max === null) {
      max = e
    } else {
      max = max > e ? max : e
    }
  }
  return max
}

function assert(condition: boolean, message?: string): asserts condition is true {
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
Number.prototype.timeFormat = function (this: number) {
  const hours = Math.floor(this / 3600)
  const minutes = Math.floor((this - (hours * 3600)) / 60)
  const seconds = Math.round(this - (hours * 3600) - (minutes * 60))

  const hourPrefix = hours == 0 ? "" : hours.withMinDigits(2)
  return hourPrefix + minutes.withMinDigits(2) + ':' + seconds.withMinDigits(2)
}

interface String {
  takeAfterLast(substring: string): string
  capitalize(): string
}

String.prototype.takeAfterLast = function (subs) {
  return this.substring(this.lastIndexOf(subs) + 1)
}
String.prototype.capitalize = function () {
  return this[0].toUpperCase() + this.slice(1)
}

interface Boolean {
  isFalse(): boolean
}

Boolean.prototype.isFalse = function () {
  return not(this.valueOf())
}

const ENTER_CODE: number = 13

class IllegalArgumentException extends Error {
  constructor(message: string) {super(message)}
}

function require(cond: boolean, message?: string): void {
  if (!cond)
    throw new IllegalArgumentException(message || "requirement failed")
}

function eventListenerToPromise(element: HTMLElement, event: string): Promise<void> {
  return new Promise((resolve, _reject) => {
    element.addEventListener(event, function () {resolve()})
  })
}

async function sleep(ms: number): Promise<void> {
  return new Promise(resolve => setTimeout(resolve, ms))
}

// Adapted from https://stackoverflow.com/a/61511955/736508
async function waitForElem(selector: string): Promise<Element> {
  return new Promise(resolve => {
    const result = document.querySelector(selector)
    if (result) {
      return resolve(result)
    }

    const observer = new MutationObserver(mutations => {
      const result = document.querySelector(selector)
      if (result) {
        observer.disconnect()
        resolve(result)
      }
    })

    observer.observe(document.documentElement, {
      childList: true,
      subtree: true,
    })
  })
}

async function toPromise(jqxhr: JQueryXHR): Promise<void> {
  return jqxhr.then(() => {}).catch((e => {throw e}))
}

interface Promise<T> {
  void(): Promise<void>
}

Promise.prototype.void = async function () {
  await this
}

function $exposeGlobally(obj: any): void {
  (window as any).obj = obj
}

function $exposeGloballyExplicit(name: string, obj: any): void {
  (window as any)[name] = obj
}

// Type checkers
function isString(e: any): e is string {
  return typeof e == "string"
}

function isObject(e: any): e is object {
  return typeof e == "object"
}

function isFunction(e: any): e is Function {
  return typeof e == "function"
}
