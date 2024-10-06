interface Song {
    readonly title: string
    readonly artistName: string
    readonly albumName: string
    readonly track: number
    readonly year: number
    readonly bitrate: string
    readonly duration: number
    readonly size: number
    readonly discNumber?: string
    readonly trackGain: number

    // Classical music fields
    readonly composer?: string
    readonly conductor?: string
    readonly opus?: string
    readonly orchestra?: string
    readonly performanceYear?: number

    // The below represent URLs
    readonly file: string
    readonly poster: string
    // Either mp3 or flac should be available
    readonly mp3?: string
    readonly flac?: string

    offline_url?: string
}

interface Album {
    artistName: string
    title: string
    year: number
    dir: string
}

abstract class Player {
    abstract load(song: Song): void
    abstract playCurrentSong(): void
    abstract stop(): void
    abstract pause(): void
    abstract isPaused(): boolean
    restart(): void {
        this.stop()
        this.playCurrentSong()
    }
    togglePause(): void {
        if (this.isPaused())
            this.playCurrentSong()
        else
            this.pause()
    }
    abstract percentageOfSongPlayed(): number
    abstract currentPlayingInSeconds(): number
    abstract setVolume(v: number): void
    abstract getVolume(): number
    abstract skip(seconds: number): void
}

interface JPlayerElement {
    jPlayer(str: String, value: any): void
    data(): any
}

(window as any).gplayer = new class extends Player {
    private player(): JPlayerElement {return $("#jquery_jplayer_1") as unknown as JPlayerElement}
    override load(song: Song): void {this.player().jPlayer("setMedia", song)}
    private click(what: string): void {$(".jp-" + what).click()}
    override pause(): void {this.click("pause")}
    override stop(): void {this.click("stop")}
    override playCurrentSong(): void {this.click("play")}
    override isPaused(): boolean {return this.player().data().jPlayer.status.paused}
    override percentageOfSongPlayed() {
        const jPlayer = this.player().data().jPlayer
        return jPlayer ? jPlayer.status.currentPercentAbsolute : undefined
    }
    override currentPlayingInSeconds(): number {
        return this.player().data().jPlayer.status.currentTime
    }
    private volumeBar() {return $(".jp-volume-bar-value")}
    override getVolume(): number {return this.volumeBar().width()}
    setVolume(v: number): void {
        this.volumeBar().width(`${v}%`)
        this.player().jPlayer("volume", v / 100.0)
    }
    override skip(seconds: number): void {this.player().jPlayer("play", seconds)}
}

abstract class Playlist {
    clear(instant: boolean): void {this.setPlaylist([], instant)}
    setPlaylist(playlist: Song[], instant: boolean): void {
        this.clear(instant)
        playlist.forEach(s => console.log(s))
        const self = this
        playlist.forEach(s => self.add(s, false))
    }
    abstract add(song: Song, playNow: boolean): void
    protected abstract _next(): void
    next(count?: number): void {
        count = count || 1
        for (let i = 0; i < count; i++)
            this._next()
    }
    abstract play(index: number): void
    abstract select(index: number): void
    abstract prev(): void
    abstract currentIndex(): number
    currentPlayingSong(): Song {return this.songs()[this.currentIndex()]}
    abstract songs(): Song[]
    last(): Song {return this.songs()[this.length() - 1]}
    length(): number {return this.songs().length}
    toString(song: Song): string {return PlaylistCustomizations.mediaMetadata(song)}
    // The list presentation reversed, so song at index 0 is actually the last song, not the first.
    getDisplayedIndex(index: number): number {return this.length() - 1 - index}
}

declare let playlist: any;
(window as any).gplaylist = makePlaylist()

function makePlaylist(): Playlist {
    function pl(): any {return playlist}

    const result = new class extends Playlist {
        override currentIndex() {return pl().current}
        override songs() {return pl().playlist}
        override add(song: Song, playNow: boolean): void {pl().add(song, playNow)}
        override _next(): void {return pl().next()}
        override prev(): void {return pl().previous()}
        override clear(): void {
            const instant = true
            pl().setPlaylist([], instant)
        }
        override play(index: number): void { pl().play(index)}
        override select(index: number): void {return pl().select(index)}
    }
    $(function (): void {pl().getDisplayedIndex = result.getDisplayedIndex})
    return result
}
