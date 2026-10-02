import {RawJSON} from "./api.js"
import {Duration, Percentage} from "./common_types.js";

export class Song {
  readonly title: string
  readonly artistName: string
  readonly albumName: string
  readonly track: number
  readonly year: number
  readonly bitrate: string
  readonly duration: Duration
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
  offlineUrl?: string

  static fromJSON(json: RawJSON): Song {
    return new Song(
      json.title,
      json.artistName,
      json.albumName,
      json.track,
      json.year,
      json.bitrate,
      Duration.fromJson(json.duration),
      json.size,
      json.discNumber,
      json.trackGain,
      json.composer,
      json.conductor,
      json.opus,
      json.orchestra,
      json.performanceYear,
      json.file,
      json.poster,
      json.mp3,
      json.flac,
    )
  }

  private constructor(
    title: string,
    artistName: string,
    albumName: string,
    track: number,
    year: number,
    bitrate: string,
    duration: Duration,
    size: number,
    discNumber: string | undefined,
    trackGain: number,
    composer: string | undefined,
    conductor: string | undefined,
    opus: string | undefined,
    orchestra: string | undefined,
    performanceYear: number | undefined,
    file: string,
    poster: string,
    mp3: string | undefined,
    flac: string | undefined,
  ) {
    this.title = title
    this.artistName = artistName
    this.albumName = albumName
    this.track = track
    this.year = year
    this.bitrate = bitrate
    this.duration = duration
    this.size = size
    this.discNumber = discNumber
    this.trackGain = trackGain

    this.composer = composer
    this.conductor = conductor
    this.opus = opus
    this.orchestra = orchestra
    this.performanceYear = performanceYear

    this.file = file
    this.poster = poster
    this.mp3 = mp3
    this.flac = flac
    assert(
      // It can be neither when it returns as search results, for example.
      this.mp3 === undefined || this.flac === undefined,
      "Song can't have both mp3 and flac",
    )
    this.offlineUrl = undefined
  }
}

export function songPath(song: Song): string {
  return song.mp3 ? song.mp3 : song.flac!
}

export type AlbumType = 'Album' | 'Live' | 'EP'

export interface Album {
  readonly artistName: string
  readonly title: string
  readonly year: number
  readonly dir: string
  readonly date: Date
  readonly albumType: AlbumType

  // Relevant for new albums
  readonly reconID?: string

  // Classical music fields
  readonly composer?: string
  readonly conductor?: string
  readonly opus?: string
  readonly orchestra?: string
  readonly performanceYear?: number
  readonly discNumbers?: string[]
}

export interface Artist {
  readonly name: string
  readonly albums: Album[]
}

export class TimeUpdate {
  readonly currentDuration: Duration
  readonly totalDuration: Duration

  constructor(input: { currentDuration: Duration, totalDuration: Duration }) {
    this.currentDuration = input.currentDuration
    this.totalDuration = input.totalDuration
  }
}

export type PlayerEvent = "ENDED" | TimeUpdate | Song

export type PlayerEventListener = (pe: PlayerEvent) => void

export abstract class Player {
  abstract load(song: Song): void
  abstract playCurrentSong(): void
  abstract stop(): void
  /** Does not unpause. */
  abstract pause(): void
  abstract isPaused(): boolean
  togglePause(): void {
    if (this.isPaused())
      this.playCurrentSong()
    else
      this.pause()
  }
  abstract percentageOfSongPlayed(): Percentage
  abstract duration(): Duration
  abstract currentTime(): Duration
  abstract setVolume(v: Volume): void
  abstract getVolume(): Volume
  abstract skipTo(duration: Duration): void
  abstract clear(): void
  abstract listen(callback: PlayerEventListener): void
  abstract unlisten(callback: PlayerEventListener): void
}

export abstract class Playlist {
  async clear(instant: boolean): Promise<void> {return this.setPlaylist([], instant)}
  async setPlaylist(playlist: Song[], instant: boolean): Promise<void> {
    await this.clear(instant)
    const that = this
    for (const s of playlist) {
      await that.add(s, false)
    }
  }
  abstract add(song: Song | Song[], playNow: boolean): Promise<void>
  protected abstract _next(): void
  next(count?: number): void {
    count = count || 1
    for (let i = 0; i < count; i++)
      this._next()
  }
  abstract play(index: number): Promise<void>
  abstract select(index: number): Promise<void>
  abstract prev(): void
  abstract currentIndex(): number
  currentPlayingSong(): Song {return this.songs()[this.currentIndex()]}
  abstract songs(): Song[]
  last(): Song {return this.songs()[this.length() - 1]}
  length(): number {return this.songs().length}
  // The list presentation reversed, so song at index 0 is actually the last song, not the first.
  getDisplayedIndex(index: number): number {return this.length() - 1 - index}
  isLastSongPlaying(): boolean {return this.currentIndex() == this.length() - 1}
  abstract removeItemAux(index: any, next: (x: JQuery<HTMLElement>) => JQuery<HTMLElement>): void
}


export class Volume {
  private readonly volume: Percentage

  private constructor(volume: Percentage) {
    this.volume = volume
  }

  static fromJSON(json: RawJSON) {return new Volume(Percentage.fromJSON(json))}
  toJSON(): RawJSON {return this.volume.toJSON()}
  static fromPercentage(p: Percentage) {return new Volume(p)}

  times(number: number): Volume {return new Volume(this.volume.times(number))}

  setWidth(volumeBar: JQuery<HTMLElement>) {
    volumeBar.css("width", `${this.volume.zeroToHundred()}%`)
  }
  setVolume(element: HTMLAudioElement): void {
    element.volume = this.volume.zeroToOne()
  }
  isMuted() {return this.volume.isZero()}

  percentage(): Percentage {return this.volume}
}

$exposeGlobally!(Duration)
$exposeGlobally!(Percentage)
$exposeGlobally!(Volume)
