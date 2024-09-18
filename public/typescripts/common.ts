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
