// A very simple module, showing the most recent album, so it can be easily added to the playlist.

declare function openConnection(url: string, handle: (s: any) => void): WebSocket

const ADD = "plus"

function write(value: JQuery<HTMLElement>): void {
    $("#last_album").empty().append(value)
}

function albumText(album: Album): string {
    return `${icon(ADD)} ${album.artistName}: ${album.title}`
}

function addAlbum(album: Album): void {
    $.get("data/albums/" + album.dir, e => gplaylist.add(e, false))
}

class LastAlbum {
    private shouldAddNextNewAlbum: boolean = false
    private lastAlbumText: string = undefined

    constructor() {
        this.openLastAlbumConnection()
        write(span("Fetching last album..."))
    }
    private updateAlbum(album: Album): void {
        const text = albumText(album)
        if (text !== this.lastAlbumText && this.shouldAddNextNewAlbum) {
            addAlbum(album)
            this.shouldAddNextNewAlbum = false
        }
        console.log(`Updating last album: '${text}'`)
        const albumElement = elem("span", text)
        write(albumElement)
        albumElement.find(".fa-" + ADD).click(() => addAlbum(album))
        if (albumElement.custom_overflown())
            albumElement.custom_tooltip(text)
        this.lastAlbumText = text
    }

    private last_album_websocket: WebSocket = undefined

    private openLastAlbumConnection(): void {
        this.last_album_websocket =
            openConnection("last_album", msg => this.updateAlbum(JSON.parse(msg.data)))
    }

    reopenLastAlbumWebsocketIfNeeded(): void {
        if (this.last_album_websocket.readyState === this.last_album_websocket.CLOSED)
            this.openLastAlbumConnection()
    }

    addNextNewAlbum(): void {this.shouldAddNextNewAlbum = true}
}

(window as any).LastAlbum = new LastAlbum()
